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
import com.tyyd.framework.dat.core.support.JobDomainConverter;
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
public class CronJobQueueApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/cron-job-get")
    public RestfulResponse cronJobGet(JobQueueReq request) {
        PaginationRsp<TaskPo> paginationRsp = appContext.getCronJobQueue().pageSelect(request);
        RestfulResponse response = new RestfulResponse();
        response.setSuccess(true);
        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        return response;
    }

    @RequestMapping("/job-queue/cron-job-update")
    public RestfulResponse cronJobUpdate(JobQueueReq request) {
        RestfulResponse response = new RestfulResponse();
        // 检查参数
        try {
            Assert.hasLength(request.getJobId(), "jobId不能为空!");
            Assert.hasLength(request.getCronExpression(), "cronExpression不能为空!");
        } catch (IllegalArgumentException e) {
            return Builder.build(false, e.getMessage());
        }
        try {
            // 1. 检测 cronExpression是否是正确的
            CronExpression expression = new CronExpression(request.getCronExpression());
            if (expression.getTimeAfter(new Date()) == null) {
                return Builder.build(false, StringUtils.format("该CronExpression={} 已经没有执行时间点! 请重新设置或者直接删除。", request.getCronExpression()));
            }
            TaskPo cronJobPo = appContext.getCronJobQueue().getJob(request.getJobId());
            boolean success = appContext.getCronJobQueue().selectiveUpdate(request);
            if (success) {
                try {
                    // 看CronExpression是否有修改,如果有修改,需要更新triggerTime
                    if (!request.getCronExpression().equals(cronJobPo.getCronExpression())) {
                        request.setTriggerTime(expression.getTimeAfter(new Date()));
                    }
                    appContext.getExecutableJobQueue().selectiveUpdate(request);
                } catch (Exception e) {
                    return Builder.build(false, "更新等待执行的任务失败，请手动更新! error:" + e.getMessage());
                }
                response.setSuccess(true);
            } else {
                return Builder.build(false, "该任务已经被删除或者执行完成");
            }
            return response;
        } catch (ParseException e) {
            return Builder.build(false, "请输入正确的 CronExpression!" + e.getMessage());
        }
    }

    @RequestMapping("/job-queue/cron-job-delete")
    public RestfulResponse cronJobDelete(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        boolean success = appContext.getCronJobQueue().remove(request.getJobId());
        if (success) {
            try {
                appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
            } catch (Exception e) {
                return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:{}" + e.getMessage());
            }
        }
        return Builder.build(true, "ok");
    }

    @RequestMapping("/job-queue/cron-job-suspend")
    public RestfulResponse cronJobSuspend(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getJobId())) {
            return Builder.build(false, "JobId 必须传!");
        }
        TaskPo jobPo = appContext.getCronJobQueue().getJob(request.getJobId());
        if (jobPo == null) {
            return Builder.build(false, "任务不存在，或者已经删除");
        }
        try {
            jobPo.setGmtModified(SystemClock.now());
            appContext.getSuspendJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            return Builder.build(false, "改任务已经被暂停, 请检查暂停队列");
        } catch (Exception e) {
            return Builder.build(false, "移动任务到暂停队列失败, error:" + e.getMessage());
        }
        try {
            appContext.getCronJobQueue().remove(request.getJobId());
        } catch (Exception e) {
            return Builder.build(false, "删除Cron任务失败，请手动删除! error:" + e.getMessage());
        }
        try {
            appContext.getExecutableJobQueue().remove(request.getTaskTrackerNodeGroup(), request.getJobId());
        } catch (Exception e) {
            return Builder.build(false, "删除等待执行的任务失败，请手动删除! error:" + e.getMessage());
        }

        // 记录日志
        JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.SUSPEND);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        appContext.getJobLogger().log(jobLogPo);

        return Builder.build(true, "ok");
    }
}
