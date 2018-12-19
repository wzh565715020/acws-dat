package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;

/**
 * 正在执行的 任务队列
 *
 */
public interface ExecutedTaskQueue extends TaskQueueInterface {

    /**
     * 入队列
     */
    boolean add(TaskPo taskPo);

    /**
     * 出队列
     */
    boolean remove(String taskId);

    /**
     * 得到某个TaskExecuter节点上正在执行的任务
     */
    List<TaskPo> getTasks(String taskExecuterIdentity);

    /**
     * 根据过期时间得到死掉的任务
     */
    List<TaskPo> getDeadTasks(long deadline);

    /**
     * 得到JobPo
     */
    TaskPo getTask(String taskTrackerNode, String taskId);

    TaskPo getTask(String taskId);
}
