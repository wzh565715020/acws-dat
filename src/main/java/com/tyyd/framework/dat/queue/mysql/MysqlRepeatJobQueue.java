package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.admin.request.JobQueueReq;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.queue.RepeatJobQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
public class MysqlRepeatJobQueue extends AbstractMysqlJobQueue implements RepeatJobQueue {

    public MysqlRepeatJobQueue(Config config) {
        super(config);
        createTable(readSqlFile("sql/mysql/lts_repeat_job_queue.sql", getTableName()));
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

    @Override
    public int incRepeatedCount(String jobId) {
        while (true) {
            TaskPo jobPo = getJob(jobId);
            if (jobPo == null) {
                return -1;
            }
            if (new UpdateSql(getSqlTemplate())
                    .update()
                    .table(getTableName())
                    .set("repeated_count", jobPo.getRepeatedCount() + 1)
                    .where("job_id = ?", jobId)
                    .and("repeated_count = ?", jobPo.getRepeatedCount())
                    .doUpdate() == 1) {
                return jobPo.getRepeatedCount() + 1;
            }
        }
    }

    private String getTableName() {
        return JobQueueUtils.REPEAT_JOB_QUEUE;
    }

}
