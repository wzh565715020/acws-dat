package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;

/**
 * 等待执行的任务队列 (可以有多个)
 *
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public interface ExecutableTaskQueue extends TaskQueueInterface{

    /**
     * 入队列
     */
    boolean add(TaskPo jobPo);

    /**
     * 出队列
     */
    boolean remove(String taskTrackerNodeGroup, String jobId);

    /**
     * reset , runnable
     */
    void resume(TaskPo jobPo);

    /**
     * 得到死任务
     */
    List<TaskPo> getDeadJob(String taskTrackerNodeGroup, long deadline);

    /**
     * 得到JobPo
     */
    TaskPo getTask(String taskTrackerNodeGroup, String taskId);
}
