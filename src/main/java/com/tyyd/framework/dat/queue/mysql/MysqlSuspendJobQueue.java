package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.SuspendTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.admin.request.TaskQueueReq;

/**
 * @author bug (357693306@qq.com) on 3/4/16.
 */
public class MysqlSuspendJobQueue extends AbstractMysqlTaskQueue implements SuspendTaskQueue {

    public MysqlSuspendJobQueue(Config config) {
        super(config);
    }

    @Override
    public boolean add(TaskPo jobPo) {
        return add(getTableName(), jobPo);
    }

    @Override
    public TaskPo getJob(String jobId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .single(RshHolder.TASK_PO_RSH);
    }

    @Override
    public boolean remove(String jobId) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .doDelete() == 1;
    }

    @Override
    protected String getTableName() {
        return TaskQueueUtils.TASK_POOL;
    }

}
