package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;

public interface TaskQueue extends TaskQueueInterface{
    /**
     * 添加任务
     *
     * @throws DupEntryException
     */
    boolean add(TaskPo taskPo);

    /**
     * 完成某一次执行，返回队列中的这条记录
     */
    TaskPo getTask(String taskId);

    /**
     * 移除Cron Job
     */
    boolean remove(String taskId);

    /**
     * 增加重复次数
     */
    int incRepeatedCount(String taskId);
}
