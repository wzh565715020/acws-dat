package com.tyyd.framework.dat.core.support;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.queue.domain.TaskPo;

public class TaskDomainConverter {

    private TaskDomainConverter() {
    }

    public static TaskPo convert(Task task) {
        TaskPo taskPo = new TaskPo();
        taskPo.setTaskId(task.getTaskId());
        taskPo.setTaskClass(task.getTaskClass());
        taskPo.setTaskExecType(task.getTaskExecType());
        taskPo.setTaskName(task.getTaskName());
        taskPo.setTaskType(task.getTaskType());
        taskPo.setCreateDate(SystemClock.now());
        taskPo.setUpdateDate(taskPo.getCreateDate());
        taskPo.setSubmitNode(task.getSubmitNode());
        taskPo.setCron(task.getCron());
        taskPo.setRepeatCount(task.getRepeatCount());
        taskPo.setCreateDate(SystemClock.now());
        taskPo.setUpdateDate(SystemClock.now());
        taskPo.setCreateUserid("");
        taskPo.setUpdateUserid("");
        if (!taskPo.isCron()) {
            if (task.getTriggerTime() == null) {
                taskPo.setTriggerTime(SystemClock.now());
            } else {
                taskPo.setTriggerTime(task.getTriggerTime());
            }
        }
        if (task.getRepeatCount() != 0) {
            taskPo.setCron(null);
            taskPo.setRepeatInterval(task.getRepeatInterval());
        }
        return taskPo;
    }

    /**
     * JobPo 转 task
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
        Task task = jobMeta.getTask();
        jobLogPo.setInternalExtParams(jobMeta.getInternalExtParams());
        jobLogPo.setSubmitNodeGroup(task.getSubmitNode());
        jobLogPo.setTaskId(task.getTaskId());
        jobLogPo.setJobId(jobMeta.getId());
        jobLogPo.setCronExpression(task.getCron());
        jobLogPo.setTriggerTime(task.getTriggerTime());

        jobLogPo.setRepeatCount(task.getRepeatCount());
        jobLogPo.setRepeatedCount(task.getRepeatedCount());
        jobLogPo.setRepeatInterval(task.getRepeatInterval());
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
