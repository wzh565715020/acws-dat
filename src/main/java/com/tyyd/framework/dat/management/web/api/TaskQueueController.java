package com.tyyd.framework.dat.management.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyyd.framework.dat.admin.request.TaskQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.JobLoggerRequest;
import com.tyyd.framework.dat.cmd.DefaultHttpCmd;
import com.tyyd.framework.dat.cmd.HttpCmd;
import com.tyyd.framework.dat.cmd.HttpCmdClient;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.cmd.HttpCmdNames;
import com.tyyd.framework.dat.core.commons.utils.Assert;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Pair;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.support.CronExpression;
import com.tyyd.framework.dat.core.support.TaskUtils;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.support.AppConfigurer;
import com.tyyd.framework.dat.management.support.I18nManager;
import com.tyyd.framework.dat.management.web.AbstractMVC;
import com.tyyd.framework.dat.management.web.support.Builder;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;
import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class TaskQueueController extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/taskQueue/getRepeatTask")
    public RestfulResponse getRepeatTask(TaskQueueReq request) {
        PaginationRsp<TaskPo> paginationRsp = appContext.getTaskQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/taskQueue/updateRepeatTask")
    public RestfulResponse updateRepeatTask(TaskQueueReq request) {
        // 检查参数
        try {
            Assert.hasLength(request.getTaskId(), "jobId不能为空!");
            Assert.notNull(request.getRepeatInterval(), "repeatInterval不能为空!");
            Assert.isTrue(request.getRepeatInterval() > 0, "repeatInterval必须大于0");
            Assert.isTrue(request.getRepeatCount() >= -1, "repeatCount必须>= -1");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        request.setCronExpression(null);
        TaskPo jobPo = appContext.getTaskQueue().getTask(request.getTaskId());
        boolean success = appContext.getTaskQueue().selectiveUpdate(request);
        if (success) {
            try {
                // 如果repeatInterval有修改,需要把triggerTime也要修改下
                if (!request.getRepeatInterval().equals(jobPo.getRepeatInterval())) {
                    long nextTriggerTime = TaskUtils.getRepeatNextTriggerTime(jobPo);
                    request.setTriggerTime(new Date(nextTriggerTime));
                }
                // 把等待执行的队列也更新一下
                appContext.getExecutableJobQueue().selectiveUpdate(request);
            } catch (Exception e) {
                return Builder.build(false, "更新等待执行的任务失败，请手动更新! error:" + e.getMessage());
            }
            return Builder.build(true);
        } else {
            return Builder.build(false, "该任务已经被删除或者执行完成");
        }
    }

    @RequestMapping("/taskQueue/deleteRepeatTask")
    public RestfulResponse deleteRepeatTask(TaskQueueReq request) {
        if (StringUtils.isEmpty(request.getTaskId())) {
            return Builder.build(false, "taskId 必须传!");
        }
        boolean success = appContext.getTaskQueue().remove(request.getTaskId());
        if (success) {
            try {
                appContext.getExecutableJobQueue().remove(request.getTaskId());
            } catch (Exception e) {
                return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:{}" + e.getMessage());
            }
        }
        return Builder.build(true);
    }

    @RequestMapping("/taskQueue/getExecutableTask")
    public RestfulResponse getExecutableTask(TaskQueueReq request) {
        PaginationRsp<TaskPo> paginationRsp = appContext.getExecutableJobQueue().pageSelect(request);

        boolean needClear = Boolean.valueOf(AppConfigurer.getProperty("lts.admin.remove.running.job.on.executable.search", "false"));
        if (needClear) {
            paginationRsp = clearRunningJob(paginationRsp);
        }
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    /**
     * 比较恶心的逻辑,当等待执行队列的任务同时也在执行中队列, 则不展示
     */
    private PaginationRsp<TaskPo> clearRunningJob(PaginationRsp<TaskPo> paginationRsp) {
        if (paginationRsp == null || paginationRsp.getResults() == 0) {
            return paginationRsp;
        }
        PaginationRsp<TaskPo> rsp = new PaginationRsp<TaskPo>();
        List<TaskPo> rows = new ArrayList<TaskPo>();
        for (TaskPo jobPo : paginationRsp.getRows()) {
            if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskExecuteNode(), jobPo.getTaskId()) == null) {
                // 没有正在执行, 则显示在等待执行列表中
                rows.add(jobPo);
            }
        }
        rsp.setRows(rows);
        rsp.setResults(paginationRsp.getResults() - paginationRsp.getRows().size() - rows.size());
        return rsp;
    }

    @RequestMapping("/taskQueue/getExecutingTask")
    public RestfulResponse getExecutingTask(TaskQueueReq request) {
        PaginationRsp<TaskPo> paginationRsp = appContext.getExecutingJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/taskQueue/updateExecutableTask")
    public RestfulResponse updateExecutableTask(TaskQueueReq request) {
        // 检查参数
        // 1. 检测 cronExpression是否是正确的
        if (StringUtils.isNotEmpty(request.getCronExpression())) {
            try {
                CronExpression expression = new CronExpression(request.getCronExpression());
                if (expression.getTimeAfter(new Date()) == null) {
                    return Builder.build(false, StringUtils.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
                }
            } catch (ParseException e) {
                return Builder.build(false, "请输入正确的 CronExpression!");
            }
        }
        try {
            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        boolean success = appContext.getExecutableJobQueue().selectiveUpdate(request);
        RestfulResponse response = new RestfulResponse();
        if (success) {
            response.setSuccess(true);
        } else {
            response.setSuccess(false);
            response.setCode("DELETE_OR_RUNNING");
        }
        return response;
    }

    @RequestMapping("/taskQueue/deleteExecutableTask")
    public RestfulResponse executableJobDelete(TaskQueueReq request) {
        try {
            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }

        boolean success = appContext.getExecutableJobQueue().remove(request.getTaskId());
        if (success) {
            if (StringUtils.isNotEmpty(request.getCronExpression())) {
                // 是Cron任务, Cron任务队列的也要被删除
                try {
                    appContext.getTaskQueue().remove(request.getTaskId());
                } catch (Exception e) {
                    return Builder.build(false, "在Cron任务队列中删除该任务失败，请手动更新! error:" + e.getMessage());
                }
            }
            return Builder.build(true);
        } else {
            return Builder.build(false, "更新失败，该条任务可能已经删除.");
        }
    }

    @RequestMapping("/job-logger/job-logger-get")
    public RestfulResponse jobLoggerGet(JobLoggerRequest request) {
        RestfulResponse response = new RestfulResponse();

        PaginationRsp<TaskLogPo> paginationRsp = appContext.getJobLogger().search(request);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());

        response.setSuccess(true);
        return response;
    }


    @RequestMapping("/taskQueue/addTask")
    public RestfulResponse jobAdd(String taskType, TaskQueueReq request) {
        // 表单check
        try {
            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
            if (request.getNeedFeedback()) {
                Assert.hasLength(request.getSubmitNode(), "submitNodeGroup不能为空!");
            }

            if (StringUtils.isNotEmpty(request.getCronExpression())) {
                try {
                    CronExpression expression = new CronExpression(request.getCronExpression());
                    Date nextTime = expression.getTimeAfter(new Date());
                    if (nextTime == null) {
                        return Builder.build(false, StringUtils.format("该CronExpression={} 已经没有执行时间点!", request.getCronExpression()));
                    } else {
                        request.setTriggerTime(nextTime);
                    }
                } catch (ParseException e) {
                    return Builder.build(false, "请输入正确的 CronExpression!");
                }
            }

        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }

        Pair<Boolean, String> pair = addJob(taskType, request);
        return Builder.build(pair.getKey(), pair.getValue());
    }

    private Pair<Boolean, String> addJob(String jobType, TaskQueueReq request) {

        Task task = new Task();
        task.setTaskId(request.getTaskId());
        if (CollectionUtils.isNotEmpty(request.getExtParams())) {
            task.setParams(JSON.toJSONString(request.getExtParams()));
        }
        // 执行节点的group名称
        task.setSubmitNode(request.getSubmitNode());
        // 这个是 cron expression 和 quartz 一样，可选
        task.setCron(request.getCronExpression());
        if (request.getTriggerTime() != null) {
            task.setTriggerTime(request.getTriggerTime().getTime());
        }
        task.setRepeatCount(request.getRepeatCount() == null ? 0 : request.getRepeatCount());
        task.setRepeatInterval(request.getRepeatInterval());

        task.setMaxRetryTimes(request.getMaxRetryTimes() == null ? 0 : request.getMaxRetryTimes());

        if ("REAL_TIME_JOB".equals(jobType)) {
            task.setCron(null);
            task.setTriggerTime(null);
            task.setRepeatInterval(null);
            task.setRepeatCount(0);
        } else if ("TRIGGER_TIME_JOB".equals(jobType)) {
            task.setCron(null);
            task.setRepeatInterval(null);
            task.setRepeatCount(0);
        } else if ("CRON_JOB".equals(jobType)) {
            task.setRepeatInterval(null);
            task.setRepeatCount(0);
        } else if ("REPEAT_JOB".equals(jobType)) {
            task.setCron(null);
        }
        return addJob(task);
    }

    private Pair<Boolean, String> addJob(Task job) {
        HttpCmd httpCmd = new DefaultHttpCmd();
        httpCmd.setCommand(HttpCmdNames.HTTP_CMD_ADD_JOB);
        httpCmd.addParam("job", JSON.toJSONString(job));

        List<Node> jobTrackerNodeList = appContext.getNodeMemCacheAccess().getNodeByNodeType(NodeType.TASK_DISPATCH);
        if (CollectionUtils.isEmpty(jobTrackerNodeList)) {
            return new Pair<Boolean, String>(false, I18nManager.getMessage("job.tracker.not.found"));
        }

        HttpCmdResponse response = null;
        for (Node node : jobTrackerNodeList) {
            httpCmd.setNodeIdentity(node.getIdentity());
            response = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), httpCmd);
            if (response.isSuccess()) {
                return new Pair<Boolean, String>(true, "Add success");
            }
        }
        if (response != null) {
            return new Pair<Boolean, String>(false, response.getMsg());
        } else {
            return new Pair<Boolean, String>(false, "Add failed");
        }
    }
}
