package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;

public interface PoolQueue extends PoolQueueInterface{
    /**
     * 添加任务
     *
     * @throws DupEntryException
     */
    boolean add(PoolPo poolPo);

    /**
     * 完成某一次执行，返回队列中的这条记录
     */
    PoolPo getPool(String poolId);

    /**
     * 移除Cron task
     */
    boolean remove(String poolId);

}
