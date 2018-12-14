package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlExecutingJobQueue extends AbstractMysqlTaskQueue implements ExecutingTaskQueue {

    public MysqlExecutingJobQueue(Config config) {
        super(config);
    }

    @Override
    public boolean add(TaskPo jobPo) {
        return super.add(getTableName(), jobPo);
    }

    @Override
    public boolean remove(String jobId) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .where("id = ?", jobId)
                .doDelete() == 1;
    }

    @Override
    public List<TaskPo> getJobs(String taskTrackerIdentity) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_tracker_identity = ?", taskTrackerIdentity)
                .list(RshHolder.TASK_PO_LIST_RSH);
    }

    @Override
    public List<TaskPo> getDeadJobs(long deadline) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("gmt_created < ?", deadline)
                .list(RshHolder.TASK_PO_LIST_RSH);
    }

    @Override
    public TaskPo getJob(String taskTrackerNodeGroup, String taskId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single(RshHolder.TASK_PO_RSH);
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
    protected String getTableName() {
        return TaskQueueUtils.EXECUTING_JOB_QUEUE;
    }
}
