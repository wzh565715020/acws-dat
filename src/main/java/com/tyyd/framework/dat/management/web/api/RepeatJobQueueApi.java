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
import com.tyyd.framework.dat.core.support.JobDomainConverter;
import com.tyyd.framework.dat.core.support.JobUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.web.AbstractMVC;
import com.tyyd.framework.dat.management.web.support.Builder;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;
import com.tyyd.framework.dat.queue.domain.JobPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;

import java.util.Date;

@RestController
public class RepeatJobQueueApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/job-queue/repeat-job-get")
    public RestfulResponse repeatJobGet(JobQueueReq request) {
        PaginationRsp<JobPo> paginationRsp = appContext.getRepeatJobQueue().pageSelect(request);
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
        JobPo jobPo = appContext.getRepeatJobQueue().getJob(request.getJobId());
        boolean success = appContext.getRepeatJobQueue().selectiveUpdate(request);
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
        boolean success = appContext.getRepeatJobQueue().remove(request.getJobId());
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
        JobPo jobPo = appContext.getRepeatJobQueue().getJob(request.getJobId());
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
            appContext.getRepeatJobQueue().remove(request.getJobId());
        } catch (Exception e) {
            return Builder.build(false, "删除Repeat任务失败，请手动删除! error:" + e.getMessage());
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

        return Builder.build(true);
    }

}
