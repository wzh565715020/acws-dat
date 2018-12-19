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

    public static TaskLogPo convertJobLog(TaskMeta taskMeta) {
        TaskLogPo jobLogPo = new TaskLogPo();
        jobLogPo.setCreateTime(SystemClock.now());
        Task task = taskMeta.getTask();
        jobLogPo.setSubmitNode(task.getSubmitNode());
        jobLogPo.setTaskId(task.getTaskId());
        jobLogPo.setId(taskMeta.getId());
        jobLogPo.setCron(task.getCron());
        jobLogPo.setTriggerTime(task.getTriggerTime());

        jobLogPo.setRepeatCount(task.getRepeatCount());
        jobLogPo.setRepeatedCount(task.getRepeatedCount());
        jobLogPo.setRepeatInterval(task.getRepeatInterval());
        return jobLogPo;
    }

    public static TaskLogPo convertJobLog(TaskPo taskPo) {
        TaskLogPo taskLogPo = new TaskLogPo();
        taskLogPo.setId(taskPo.getId());
        taskLogPo.setCreateTime(SystemClock.now());
        taskLogPo.setSubmitNode(taskPo.getSubmitNode());
        taskLogPo.setTaskId(taskPo.getTaskId());
        taskLogPo.setCron(taskPo.getCron());
        taskLogPo.setTriggerTime(taskPo.getTriggerTime());
        taskLogPo.setTaskExecuteNode(taskPo.getTaskExecuteNode());
        taskLogPo.setRepeatCount(taskPo.getRepeatCount());
        taskLogPo.setRepeatedCount(taskPo.getRepeatedCount());
        taskLogPo.setRepeatInterval(taskPo.getRepeatInterval());
        return taskLogPo;
    }

}
