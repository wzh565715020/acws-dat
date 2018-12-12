package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;

/**
 * 正在执行的 任务队列
 *
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public interface ExecutingTaskQueue extends QueueInterface {

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
    List<TaskPo> getJobs(String taskTrackerIdentity);

    /**
     * 根据过期时间得到死掉的任务
     */
    List<TaskPo> getDeadJobs(long deadline);

    /**
     * 得到JobPo
     */
    TaskPo getJob(String taskTrackerNodeGroup, String taskId);

    TaskPo getJob(String jobId);
}
