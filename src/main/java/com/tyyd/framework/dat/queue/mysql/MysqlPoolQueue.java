package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.PoolQueue;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;

public class MysqlPoolQueue extends AbstractMysqlPoolQueue implements PoolQueue {

    public MysqlPoolQueue(Config config) {
        super(config);
    }

    @Override
    public boolean add(PoolPo poolPo) {
        return super.add(getTableName(), poolPo);
    }

    @Override
    public PoolPo getPool(String jobId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("pool_id = ?", jobId)
                .single(RshHolder.POOL_PO_RSH);
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

}
