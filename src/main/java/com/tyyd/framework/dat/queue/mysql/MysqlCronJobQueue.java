package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.queue.CronJobQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.admin.request.JobQueueReq;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public class MysqlCronJobQueue extends AbstractMysqlJobQueue implements CronJobQueue {

    public MysqlCronJobQueue(Config config) {
        super(config);
        createTable(readSqlFile("sql/mysql/lts_cron_job_queue.sql", getTableName()));
    }

    @Override
    protected String getTableName(JobQueueReq request) {
        return getTableName();
    }

    @Override
    public boolean add(TaskPo jobPo) {
        return super.add(getTableName(), jobPo);
    }

    @Override
    public TaskPo getJob(String jobId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .single(RshHolder.JOB_PO_RSH);
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
    public TaskPo getJob(String taskTrackerNodeGroup, String taskId) {

        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single(RshHolder.JOB_PO_RSH);
    }

    private String getTableName() {
        return JobQueueUtils.CRON_JOB_QUEUE;
    }

}
