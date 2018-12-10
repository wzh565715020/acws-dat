package com.tyyd.framework.dat.management.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyyd.framework.dat.admin.request.JobQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.commons.utils.Assert;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.support.CronExpression;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.JobDomainConverter;
import com.tyyd.framework.dat.core.support.JobUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.web.AbstractMVC;
import com.tyyd.framework.dat.management.web.support.Builder;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;

import java.text.ParseException;
import java.util.Date;

@RestController
public class SuspendJobQueueApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/suspend-job-get")
    public RestfulResponse suspendJobGet(JobQueueReq request) {
        PaginationRsp<TaskPo> paginationRsp = appContext.getSuspendJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/job-queue/suspend-job-update")
    public RestfulResponse suspendJobUpdate(String jobType, JobQueueReq request) {
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(jobType, "jobType不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        try {

            TaskPo jobPo = appContext.getSuspendJobQueue().getJob(request.getJobId());

            if ("CRON".equals(jobType)) {
                // 检查参数
                try {
                    Assert.hasLength(request.getCronExpression(), "cronExpression不能为空!");
                } catch (IllegalArgumentException e) {
                    return Builder.build(false, e.getMessage());
                }
                // 1. 检测 cronExpression是否是正确的
                CronExpression expression = new CronExpression(request.getCronExpression());
                if (expression.getTimeAfter(new Date()) == null) {
                    return Builder.build(false, StringUtils.format("该CronExpression={} 已经没有执行时间点! 请重新设置或者直接删除。", request.getCronExpression()));
                }
                // 看CronExpression是否有修改,如果有修改,需要更新triggerTime
                if (!request.getCronExpression().equals(jobPo.getCronExpression())) {
                    request.setTriggerTime(expression.getTimeAfter(new Date()));
                }
            } else {
                try {
                    Assert.notNull(request.getRepeatInterval(), "repeatInterval不能为空!");
                    Assert.isTrue(request.getRepeatInterval() > 0, "repeatInterval必须大于0");
                    Assert.isTrue(request.getRepeatCount() >= -1, "repeatCount必须>= -1");
                } catch (IllegalArgumentException e) {
                    return Builder.build(false, e.getMessage());
                }
                // 如果repeatInterval有修改,需要把triggerTime也要修改下
                if (!request.getRepeatInterval().equals(jobPo.getRepeatInterval())) {
                    long nextTriggerTime = JobUtils.getRepeatNextTriggerTime(jobPo);
                    request.setTriggerTime(new Date(nextTriggerTime));
                }
                request.setCronExpression(null);
            }

            boolean success = appContext.getSuspendJobQueue().selectiveUpdate(request);
            if (success) {
                return Builder.build(true);
            } else {
                return Builder.build(false, "该任务已经被删除或者执行完成");
            }
        } catch (ParseException e) {
            return Builder.build(false, "请输入正确的 CronExpression");
        }
    }


    @RequestMapping("/job-queue/suspend-job-delete")
    public RestfulResponse suspendJobDelete(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        boolean success = appContext.getSuspendJobQueue().remove(request.getJobId());
        return Builder.build(success);
    }


    @RequestMapping("/job-queue/suspend-job-recovery")
    public RestfulResponse suspendJobRecovery(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }

        TaskPo jobPo = appContext.getSuspendJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(false, "任务不存在，或者已经删除");
        }

        // 判断是Cron任务还是Repeat任务
        if (jobPo.isCron()) {
            // 先恢复,才能删除
            Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
            if (nextTriggerTime != null) {
                jobPo.setGmtModified(SystemClock.now());
                try {
                    // 1.add to cron job queue
                    appContext.getCronJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    return Builder.build(false, "Cron队列中任务已经存在，请检查");
                } catch (Exception e) {
                    return Builder.build(false, "插入Cron队列中任务错误, error:" + e.getMessage());
                }

                try {
                    // 2. add to executable queue
                    jobPo.setTriggerTime(nextTriggerTime.getTime());
                    appContext.getExecutableJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    return Builder.build(false, "等待执行队列中任务已经存在，请检查");
                } catch (Exception e) {
                    return Builder.build(false, "插入等待执行队列中任务错误, error:" + e.getMessage());
                }
            } else {
                return Builder.build(false, "该任务已经无效, 或者已经没有下一轮执行时间点, 请直接删除");
            }
        } else if (jobPo.isRepeatable()) {
            // 先恢复,才能删除
            if (jobPo.getRepeatedCount() < jobPo.getRepeatCount()) {
                jobPo.setGmtModified(SystemClock.now());
                try {
                    // 1.add to cron job queue
                    appContext.getRepeatJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    return Builder.build(false, "Repeat队列中任务已经存在，请检查");
                } catch (Exception e) {
                    return Builder.build(false, "插入Repeat队列中任务错误, error:" + e.getMessage());
                }

                try {
                    // 2. add to executable queue
                    TaskPo repeatJob = appContext.getRepeatJobQueue().getJob(request.getJobId());
                    long nextTriggerTime = JobUtils.getRepeatNextTriggerTime(repeatJob);
                    jobPo.setTriggerTime(nextTriggerTime);
                    appContext.getExecutableJobQueue().add(jobPo);
                } catch (DupEntryException e) {
                    return Builder.build(false, "等待执行队列中任务已经存在，请检查");
                } catch (Exception e) {
                    return Builder.build(false, "插入等待执行队列中任务错误, error:" + e.getMessage());
                }
            } else {
                return Builder.build(false, "该任务已经无效, 或者已经没有下一轮执行时间点, 请直接删除");
            }
        }

        // 从暂停表中移除
        if (!appContext.getSuspendJobQueue().remove(request.getJobId())) {
            return Builder.build(false, "恢复暂停任务失败，请重试");
        }

        // 记录日志
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.RESUME);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        appContext.getJobLogger().log(jobLogPo);

        return Builder.build(true);
    }
}
