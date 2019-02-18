package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;

/**
 * 正在执行的 任务队列
 *
 */
public interface ExecutingTaskQueue extends TaskQueueInterface {

    /**
     * 入队列
     */
    boolean add(TaskPo jobPo);

    /**
     * 出队列
     */
    boolean remove(String jobId);

    /**
     * 得到某个TaskTracker节点上正在执行的任务
     */
    List<TaskPo> getTasks(String taskTrackerIdentity);

    /**
     * 根据过期时间得到死掉的任务
     */
    List<TaskPo> getDeadTasks(long deadline);

    /**
     * 得到JobPo
     */
    List<TaskPo> getTaskByTaskId(String taskId);

    TaskPo getTask(String id);
    
}
