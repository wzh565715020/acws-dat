package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.queue.TaskFeedbackQueue;
import com.tyyd.framework.dat.queue.domain.JobFeedbackPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.JdbcAbstractAccess;
import com.tyyd.framework.dat.store.jdbc.builder.*;

import java.util.List;

public class MysqlJobFeedbackQueue extends JdbcAbstractAccess implements TaskFeedbackQueue {

    public MysqlJobFeedbackQueue(Config config) {
        super(config);
    }

    private String getTableName(String jobClientNodeGroup) {
        return JobQueueUtils.getFeedbackQueueName(jobClientNodeGroup);
    }

    @Override
    public boolean add(List<JobFeedbackPo> jobFeedbackPos) {
        if (CollectionUtils.isEmpty(jobFeedbackPos)) {
            return true;
        }
        // insert ignore duplicate record
        for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
            String jobClientNodeGroup = jobFeedbackPo.getJobRunResult().getTaskMeta().getJob().getSubmitNodeGroup();
            new InsertSql(getSqlTemplate())
                    .insertIgnore(getTableName(jobClientNodeGroup))
                    .columns("gmt_created", "job_result")
                    .values(jobFeedbackPo.getGmtCreated(), JSON.toJSONString(jobFeedbackPo.getJobRunResult()))
                    .doInsert();
        }
        return true;
    }

    @Override
    public boolean remove(String jobClientNodeGroup, String id) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName(jobClientNodeGroup))
                .where("id = ?", id)
                .doDelete() == 1;
    }

    @Override
    public long getCount(String jobClientNodeGroup) {
        return ((Long) new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName(jobClientNodeGroup))
                .single()).intValue();
    }

    @Override
    public List<JobFeedbackPo> fetchTop(String jobClientNodeGroup, int top) {

        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName(jobClientNodeGroup))
                .orderBy()
                .column("gmt_created", OrderByType.ASC)
                .limit(0, top)
                .list(RshHolder.JOB_FEED_BACK_LIST_RSH);
    }


}
