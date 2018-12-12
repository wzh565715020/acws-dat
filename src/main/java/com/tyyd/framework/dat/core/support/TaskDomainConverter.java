package com.tyyd.framework.dat.core.support;

import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.queue.domain.JobFeedbackPo;
import com.tyyd.framework.dat.queue.domain.TaskPo;

public class TaskDomainConverter {

    private TaskDomainConverter() {
    }

    public static TaskPo convert(Task job) {
        TaskPo jobPo = new TaskPo();
        jobPo.setTaskId(job.getTaskId());
        jobPo.setCreateDate(SystemClock.now());
        jobPo.setUpdateDate(jobPo.getCreateDate());
        jobPo.setSubmitNode(job.getSubmitNode());
        jobPo.setCron(job.getCron());
        jobPo.setRepeatCount(job.getRepeatCount());
        if (!jobPo.isCron()) {
            if (job.getTriggerTime() == null) {
                jobPo.setTriggerTime(SystemClock.now());
            } else {
                jobPo.setTriggerTime(job.getTriggerTime());
            }
        }
        if (job.getRepeatCount() != 0) {
            jobPo.setCron(null);
            jobPo.setRepeatInterval(job.getRepeatInterval());
        }
        return jobPo;
    }

    /**
     * JobPo 转 Job
     */
    public static TaskMeta convert(TaskPo jobPo) {
        Task job = new Task();
        job.setSubmitNode(jobPo.getSubmitNode());
        job.setTaskId(jobPo.getTaskId());
        job.setCron(jobPo.getCron());
        job.setTriggerTime(jobPo.getTriggerTime());
        job.setRepeatCount(jobPo.getRepeatCount());
        job.setRepeatedCount(jobPo.getRepeatedCount());
        job.setRepeatInterval(jobPo.getRepeatInterval());
        TaskMeta jobMeta = new TaskMeta();
        jobMeta.setTaskId(jobPo.getTaskId());
        jobMeta.setJob(job);
        return jobMeta;
    }

    public static JobLogPo convertJobLog(TaskMeta jobMeta) {
        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setGmtCreated(SystemClock.now());
        Task job = jobMeta.getJob();
        jobLogPo.setInternalExtParams(jobMeta.getInternalExtParams());
        jobLogPo.setSubmitNodeGroup(job.getSubmitNode());
        jobLogPo.setTaskId(job.getTaskId());
        jobLogPo.setJobId(jobMeta.getTaskId());
        jobLogPo.setCronExpression(job.getCron());
        jobLogPo.setTriggerTime(job.getTriggerTime());

        jobLogPo.setRepeatCount(job.getRepeatCount());
        jobLogPo.setRepeatedCount(job.getRepeatedCount());
        jobLogPo.setRepeatInterval(job.getRepeatInterval());
        return jobLogPo;
    }

    public static JobLogPo convertJobLog(TaskPo jobPo) {
        JobLogPo jobLogPo = new JobLogPo();
        jobLogPo.setGmtCreated(SystemClock.now());
        jobLogPo.setSubmitNodeGroup(jobPo.getSubmitNode());
        jobLogPo.setTaskId(jobPo.getTaskId());
        jobLogPo.setCronExpression(jobPo.getCron());
        jobLogPo.setTriggerTime(jobPo.getTriggerTime());
        jobLogPo.setTaskTrackerIdentity(jobPo.getTaskExecuteNode());

        jobLogPo.setRepeatCount(jobPo.getRepeatCount());
        jobLogPo.setRepeatedCount(jobPo.getRepeatedCount());
        jobLogPo.setRepeatInterval(jobPo.getRepeatInterval());
        return jobLogPo;
    }

    public static JobFeedbackPo convert(TaskRunResult result) {
        JobFeedbackPo jobFeedbackPo = new JobFeedbackPo();
        jobFeedbackPo.setJobRunResult(result);
        jobFeedbackPo.setId(StringUtils.generateUUID());
        jobFeedbackPo.setGmtCreated(SystemClock.now());
        return jobFeedbackPo;
    }

}
