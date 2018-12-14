package com.tyyd.framework.dat.core.support;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.queue.domain.TaskPo;

public class TaskDomainConverter {

    private TaskDomainConverter() {
    }

    public static TaskPo convert(Task job) {
        TaskPo taskPo = new TaskPo();
        taskPo.setTaskId(job.getTaskId());
        taskPo.setTaskClass(job.getTaskClass());
        taskPo.setTaskExecType(job.getTaskExecType());
        taskPo.setTaskName(job.getTaskName());
        taskPo.setTaskType(job.getTaskType());
        taskPo.setCreateDate(SystemClock.now());
        taskPo.setUpdateDate(taskPo.getCreateDate());
        taskPo.setSubmitNode(job.getSubmitNode());
        taskPo.setCron(job.getCron());
        taskPo.setRepeatCount(job.getRepeatCount());
        taskPo.setCreateDate(SystemClock.now());
        taskPo.setUpdateDate(SystemClock.now());
        taskPo.setCreateUserid("");
        taskPo.setUpdateUserid("");
        if (!taskPo.isCron()) {
            if (job.getTriggerTime() == null) {
                taskPo.setTriggerTime(SystemClock.now());
            } else {
                taskPo.setTriggerTime(job.getTriggerTime());
            }
        }
        if (job.getRepeatCount() != 0) {
            taskPo.setCron(null);
            taskPo.setRepeatInterval(job.getRepeatInterval());
        }
        return taskPo;
    }

    /**
     * JobPo 转 Job
     */
    public static TaskMeta convert(TaskPo taskPo) {
        Task task = new Task();
        task.setSubmitNode(taskPo.getSubmitNode());
        task.setTaskId(taskPo.getTaskId());
        task.setTaskClass(taskPo.getTaskClass());
        task.setTaskExecType(taskPo.getTaskExecType());
        task.setTaskName(taskPo.getTaskName());
        task.setTaskType(taskPo.getTaskType());
        task.setPoolId(taskPo.getPoolId());
        task.setCron(taskPo.getCron());
        task.setTriggerTime(taskPo.getTriggerTime());
        task.setRepeatCount(taskPo.getRepeatCount());
        task.setRepeatedCount(taskPo.getRepeatedCount());
        task.setRepeatInterval(taskPo.getRepeatInterval());
        TaskMeta taskMeta = new TaskMeta();
        taskMeta.setId(taskPo.getId());
        taskMeta.setTask(task);
        return taskMeta;
    }

    public static TaskLogPo convertJobLog(TaskMeta jobMeta) {
        TaskLogPo jobLogPo = new TaskLogPo();
        jobLogPo.setGmtCreated(SystemClock.now());
        Task job = jobMeta.getTask();
        jobLogPo.setInternalExtParams(jobMeta.getInternalExtParams());
        jobLogPo.setSubmitNodeGroup(job.getSubmitNode());
        jobLogPo.setTaskId(job.getTaskId());
        jobLogPo.setJobId(jobMeta.getId());
        jobLogPo.setCronExpression(job.getCron());
        jobLogPo.setTriggerTime(job.getTriggerTime());

        jobLogPo.setRepeatCount(job.getRepeatCount());
        jobLogPo.setRepeatedCount(job.getRepeatedCount());
        jobLogPo.setRepeatInterval(job.getRepeatInterval());
        return jobLogPo;
    }

    public static TaskLogPo convertJobLog(TaskPo jobPo) {
        TaskLogPo jobLogPo = new TaskLogPo();
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

}
