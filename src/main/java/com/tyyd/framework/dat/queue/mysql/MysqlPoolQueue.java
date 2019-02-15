package com.tyyd.framework.dat.queue.mysql;

import java.util.List;

import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.PoolQueue;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;


public  class MysqlPoolQueue extends AbstractMysqlPoolQueue implements PoolQueue {

    @Override
    public boolean add(PoolPo poolPo) {
        return super.add(getTableName(), poolPo);
    }

    @Override
    public PoolPo getPool(String poolId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("pool_id = ?", poolId)
                .single(RshHolder.POOL_PO_RSH);
    }
    @Override
    public List<PoolPo> getPoolByNodeId(String nodeId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("node_id = ?", nodeId)
                .list(RshHolder.POOL_PO_LIST_RSH);
    }
    @Override
    public boolean remove(String poolId) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .where("pool_id = ?", poolId)
                .doDelete() == 1;
    }


    @Override
    protected String getTableName() {
        return TaskQueueUtils.TASK_POOL;
    }
    
    @Override
    public boolean clearNodeByNodeId(PoolQueueReq request) {
		if (StringUtils.isEmpty(request.getNodeId())) {
			throw new JdbcException("Only allow update by nodeId");
		}
		return new UpdateSql(getSqlTemplate()).update().table(getTableName())
				.setOnNotNull("node_id", "")
				.setOnNotNull("update_date", request.getUpdateDate())
				.setOnNotNull("update_userid", request.getUpdateUserId()).where("node_id = ?", request.getNodeId())
				.doUpdate() >= 1;
	}
    
    @Override
	public boolean changeAvailableCount(PoolQueueReq request) {
		if (StringUtils.isEmpty(request.getPoolId())) {
			throw new JdbcException("Only allow update by poolId");
		}
		return new UpdateSql(getSqlTemplate()).update().table(getTableName())
				.setOnNotNull("available_count", "available_count + (" + request.getChangeAvailableCount() + ")")
				.setOnNotNull("update_date", request.getUpdateDate())
				.setOnNotNull("update_userid", request.getUpdateUserId()).where("pool_id = ?", request.getPoolId())
				.doUpdate() >= 1;
	}
	
    @Override
	public boolean decreaseAvailableCount(PoolQueueReq request) {
		if (StringUtils.isEmpty(request.getPoolId())) {
			throw new JdbcException("Only allow update by poolId");
		}
		return new UpdateSql(getSqlTemplate()).update().table(getTableName())
				.setOnNotNull("available_count", "available_count + (" + request.getChangeAvailableCount() + ")")
				.setOnNotNull("update_date", request.getUpdateDate())
				.setOnNotNull("update_userid", request.getUpdateUserId()).where("pool_id = ?", request.getPoolId()).where("and available_count > 0")
				.doUpdate() == 1;
	}
    @Override
    public List<PoolPo> getPoolGreaterAverage(int average) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("node_id in (select node_id from " + getTableName() +  " group by node_id" + " having count(*) > " + average)
                .list(RshHolder.POOL_PO_LIST_RSH);
    }
    @Override
    public List<PoolPo> getUndistributedPool() {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("node_id is null or node_id = ''")
                .list(RshHolder.POOL_PO_LIST_RSH);
    }
}
