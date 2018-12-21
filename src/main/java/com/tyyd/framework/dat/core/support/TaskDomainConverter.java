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
        taskPo.setParams(task.getParams());
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
        taskMeta.setTaskExecuteNode(taskPo.getTaskExecuteNode());
        taskMeta.setTask(task);
        return taskMeta;
    }

    public static TaskLogPo convertTaskLog(TaskMeta taskMeta) {
        TaskLogPo taskLogPo = new TaskLogPo();
        taskLogPo.setCreateTime(SystemClock.now());
        Task task = taskMeta.getTask();
        taskLogPo.setSubmitNode(task.getSubmitNode());
        taskLogPo.setTaskId(task.getTaskId());
        taskLogPo.setTaskClass(task.getTaskClass());
        taskLogPo.setTaskExecType(task.getTaskExecType());
        taskLogPo.setTaskName(task.getTaskName());
        taskLogPo.setTaskType(task.getTaskType());
        taskLogPo.setId(taskMeta.getId());
        taskLogPo.setCron(task.getCron());
        taskLogPo.setTaskExecuteNode(taskMeta.getTaskExecuteNode());
        taskLogPo.setParams(task.getParams());
        taskLogPo.setTriggerTime(task.getTriggerTime());
        taskLogPo.setRepeatCount(task.getRepeatCount());
        taskLogPo.setRepeatedCount(task.getRepeatedCount());
        taskLogPo.setRepeatInterval(task.getRepeatInterval());
        return taskLogPo;
    }

    public static TaskLogPo convertTaskLog(TaskPo taskPo) {
        TaskLogPo taskLogPo = new TaskLogPo();
        taskLogPo.setId(taskPo.getId());
        taskLogPo.setCreateTime(SystemClock.now());
        taskLogPo.setSubmitNode(taskPo.getSubmitNode());
        taskLogPo.setTaskId(taskPo.getTaskId());
        taskLogPo.setTaskClass(taskPo.getTaskClass());
        taskLogPo.setTaskExecType(taskPo.getTaskExecType());
        taskLogPo.setTaskName(taskPo.getTaskName());
        taskLogPo.setTaskType(taskPo.getTaskType());
        taskLogPo.setCron(taskPo.getCron());
        taskLogPo.setTriggerTime(taskPo.getTriggerTime());
        taskLogPo.setTaskExecuteNode(taskPo.getTaskExecuteNode());
        taskLogPo.setRepeatCount(taskPo.getRepeatCount());
        taskLogPo.setRepeatedCount(taskPo.getRepeatedCount());
        taskLogPo.setRepeatInterval(taskPo.getRepeatInterval());
        return taskLogPo;
    }

}
