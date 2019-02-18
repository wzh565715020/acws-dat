package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;

/**
 * 等待执行的任务队列 (可以有多个)
 *
 */
public interface ExecutableTaskQueue extends TaskQueueInterface{

    /**
     * 入队列
     */
    boolean add(TaskPo taskPo);

    /**
     * 出队列
     */
    boolean remove(String id);

    /**
     * reset , runnable
     */
    void resume(TaskPo taskPo);

    /**
     * 得到死任务
     */
    List<TaskPo> getDeadJob(long deadline);
    
    List<TaskPo> getTaskByTaskId(String taskId);

    /**
     * 得到JobPo
     */
    TaskPo getTask(String id);
}
