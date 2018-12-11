package com.tyyd.framework.dat.management.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyyd.framework.dat.admin.request.JobQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.JobLoggerRequest;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
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
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.domain.Pair;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.CronExpression;
import com.tyyd.framework.dat.core.support.JobUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.support.AppConfigurer;
import com.tyyd.framework.dat.management.support.I18nManager;
import com.tyyd.framework.dat.management.web.AbstractMVC;
import com.tyyd.framework.dat.management.web.support.Builder;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
public class TaskQueueController extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/repeat-job-get")
    public RestfulResponse repeatJobGet(JobQueueReq request) {
        PaginationRsp<TaskPo> paginationRsp = appContext.getTaskQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/job-queue/repeat-job-update")
    public RestfulResponse repeatJobUpdate(JobQueueReq request) {
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.notNull(request.getRepeatInterval(), "repeatInterval不能为空!");
            Assert.isTrue(request.getRepeatInterval() > 0, "repeatInterval必须大于0");
            Assert.isTrue(request.getRepeatCount() >= -1, "repeatCount必须>= -1");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        request.setCronExpression(null);
        TaskPo jobPo = appContext.getTaskQueue().getJob(request.getJobId());
        boolean success = appContext.getTaskQueue().selectiveUpdate(request);
        if (success) {
            try {
                // 如果repeatInterval有修改,需要把triggerTime也要修改下
                if (!request.getRepeatInterval().equals(jobPo.getRepeatInterval())) {
                    long nextTriggerTime = JobUtils.getRepeatNextTriggerTime(jobPo);
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

    @RequestMapping("/job-queue/repeat-job-delete")
    public RestfulResponse repeatJobDelete(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        boolean success = appContext.getTaskQueue().remove(request.getJobId());
        if (success) {
            try {
                appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
            } catch (Exception e) {
                return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:{}" + e.getMessage());
            }
        }
        return Builder.build(true);
    }

    @RequestMapping("/job-queue/repeat-job-suspend")
    public RestfulResponse repeatJobSuspend(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        TaskPo jobPo = appContext.getTaskQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(false, "任务不存在，或者已经删除");
        }
        try {
            jobPo.setGmtModified(SystemClock.now());
            appContext.getSuspendJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            return Builder.build(false, "该任务已经被暂停, 请检查暂停队列");
        } catch (Exception e) {
            return Builder.build(false, "移动任务到暂停队列失败, error:" + e.getMessage());
        }
        try {
            appContext.getTaskQueue().remove(request.getJobId());
        } catch (Exception e) {
            return Builder.build(false, "删除Repeat任务失败，请手动删除! error:" + e.getMessage());
        }
        try {
            appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
        } catch (Exception e) {
            return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:" + e.getMessage());
        }

        // 记录日志
        JobLogPo jobLogPo = TaskDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.SUSPEND);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        appContext.getJobLogger().log(jobLogPo);

        return Builder.build(true);
    }
    @RequestMapping("/job-queue/executable-job-get")
    public RestfulResponse executableJobGet(JobQueueReq request) {
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
            if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) == null) {
                // 没有正在执行, 则显示在等待执行列表中
                rows.add(jobPo);
            }
        }
        rsp.setRows(rows);
        rsp.setResults(paginationRsp.getResults() - paginationRsp.getRows().size() - rows.size());
        return rsp;
    }

    @RequestMapping("/job-queue/executing-job-get")
    public RestfulResponse executingJobGet(JobQueueReq request) {
        PaginationRsp<TaskPo> paginationRsp = appContext.getExecutingJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/job-queue/executable-job-update")
    public RestfulResponse executableJobUpdate(JobQueueReq request) {
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
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
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

    @RequestMapping("/job-queue/executable-job-delete")
    public RestfulResponse executableJobDelete(JobQueueReq request) {
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }

        boolean success = appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
        if (success) {
            if (StringUtils.isNotEmpty(request.getCronExpression())) {
                // 是Cron任务, Cron任务队列的也要被删除
                try {
                    appContext.getTaskQueue().remove(request.getJobId());
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

        PaginationRsp<JobLogPo> paginationRsp = appContext.getJobLogger().search(request);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());

        response.setSuccess(true);
        return response;
    }

    /**
     * 给JobTracker发消息 加载任务到内存
     */
    @RequestMapping("/job-queue/load-add")
    public RestfulResponse loadJob(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();

        String nodeGroup = request.getTaskTrackerNodeGroup();

        HttpCmd httpCmd = new DefaultHttpCmd();
        httpCmd.setCommand(HttpCmdNames.HTTP_CMD_LOAD_JOB);
        httpCmd.addParam("nodeGroup", nodeGroup);

        List<Node> jobTrackerNodeList = appContext.getNodeMemCacheAccess().getNodeByNodeType(NodeType.TASK_DISPATCH);
        if (CollectionUtils.isEmpty(jobTrackerNodeList)) {
            response.setMsg(I18nManager.getMessage("job.tracker.not.found"));
            response.setSuccess(false);
            return response;
        }

        boolean success = false;
        HttpCmdResponse cmdResponse = null;
        for (Node node : jobTrackerNodeList) {
            // 所有的JobTracker都load一遍
            httpCmd.setNodeIdentity(node.getIdentity());
            cmdResponse = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), httpCmd);
            if (cmdResponse.isSuccess()) {
                success = true;
            }
        }
        if (success) {
            response.setMsg("Load success");
        } else {
            response.setMsg("Load failed");
        }
        response.setSuccess(success);
        return response;
    }

    @RequestMapping("/job-queue/job-add")
    public RestfulResponse jobAdd(String jobType, JobQueueReq request) {
        // 表单check

        try {
            Assert.hasLength(request.getTaskId(), "taskId不能为空!");
            Assert.hasLength(request.getTaskTrackerNodeGroup(), "taskTrackerNodeGroup不能为空!");
            if (request.getNeedFeedback()) {
                Assert.hasLength(request.getSubmitNodeGroup(), "submitNodeGroup不能为空!");
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

        Pair<Boolean, String> pair = addJob(jobType, request);
        return Builder.build(pair.getKey(), pair.getValue());
    }

    private Pair<Boolean, String> addJob(String jobType, JobQueueReq request) {

        Task job = new Task();
        job.setTaskId(request.getTaskId());
        if (CollectionUtils.isNotEmpty(request.getExtParams())) {
            for (Map.Entry<String, String> entry : request.getExtParams().entrySet()) {
                job.setParam(entry.getKey(), entry.getValue());
            }
        }
        // 执行节点的group名称
        job.setTaskTrackerNodeGroup(request.getTaskTrackerNodeGroup());
        job.setSubmitNodeGroup(request.getSubmitNodeGroup());

        job.setNeedFeedback(request.getNeedFeedback());
        job.setReplaceOnExist(true);

        // 这个是 cron expression 和 quartz 一样，可选
        job.setCronExpression(request.getCronExpression());
        if (request.getTriggerTime() != null) {
            job.setTriggerTime(request.getTriggerTime().getTime());
        }
        job.setRepeatCount(request.getRepeatCount() == null ? 0 : request.getRepeatCount());
        job.setRepeatInterval(request.getRepeatInterval());

        job.setPriority(request.getPriority());
        job.setMaxRetryTimes(request.getMaxRetryTimes() == null ? 0 : request.getMaxRetryTimes());

        if ("REAL_TIME_JOB".equals(jobType)) {
            job.setCronExpression(null);
            job.setTriggerTime(null);
            job.setRepeatInterval(null);
            job.setRepeatCount(0);
        } else if ("TRIGGER_TIME_JOB".equals(jobType)) {
            job.setCronExpression(null);
            job.setRepeatInterval(null);
            job.setRepeatCount(0);
        } else if ("CRON_JOB".equals(jobType)) {
            job.setRepeatInterval(null);
            job.setRepeatCount(0);
        } else if ("REPEAT_JOB".equals(jobType)) {
            job.setCronExpression(null);
        }
        return addJob(job);
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

    @RequestMapping("/job-queue/executing-job-terminate")
    public RestfulResponse jobTerminate(String jobId) {

        TaskPo jobPo = appContext.getExecutingJobQueue().getJob(jobId);
        if (jobPo == null) {
            return Builder.build(false, "该任务已经执行完成或者被删除");
        }

        String taskTrackerIdentity = jobPo.getTaskTrackerIdentity();

        Node node = appContext.getNodeMemCacheAccess().getNodeByIdentity(taskTrackerIdentity);
        if (node == null) {
            return Builder.build(false, "执行该任务的TaskTracker已经离线");
        }

        HttpCmd cmd = new DefaultHttpCmd();
        cmd.setCommand(HttpCmdNames.HTTP_CMD_JOB_TERMINATE);
        cmd.setNodeIdentity(taskTrackerIdentity);
        cmd.addParam("jobId", jobId);
        HttpCmdResponse response = HttpCmdClient.doPost(node.getIp(), node.getHttpCmdPort(), cmd);
        if (response.isSuccess()) {
            return Builder.build(true);
        } else {
            return Builder.build(false, response.getMsg());
        }
    }
}
