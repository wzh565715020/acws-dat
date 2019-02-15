package com.tyyd.framework.dat.queue;

import java.util.List;

import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;

public interface PoolQueue extends PoolQueueInterface{
    /**
     * 添加任务
     *
     * @throws DupEntryException
     */
	public boolean add(PoolPo poolPo);

    /**
     * 完成某一次执行，返回队列中的这条记录
     */
    public PoolPo getPool(String poolId);
    
    public List<PoolPo> getPoolByNodeId(String nodeId);
    /**
     * 移除Cron task
     */
    public boolean remove(String poolId);
    
    public boolean clearNodeByNodeId(PoolQueueReq request);
    
    public boolean changeAvailableCount(PoolQueueReq request);
    
    public boolean decreaseAvailableCount(PoolQueueReq request);
    
    public List<PoolPo> getPoolGreaterAverage(int average);
    
    public List<PoolPo> getUndistributedPool();
}
