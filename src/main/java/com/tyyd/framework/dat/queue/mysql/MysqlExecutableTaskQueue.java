package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.admin.request.JobQueueReq;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;

import java.util.List;

public class MysqlExecutableTaskQueue extends AbstractMysqlTaskQueue implements ExecutableTaskQueue {

    public MysqlExecutableTaskQueue(Config config) {
        super(config);
    }

    @Override
    protected String getTableName(JobQueueReq request) {
        if (StringUtils.isEmpty(request.getTaskTrackerNodeGroup())) {
            throw new IllegalArgumentException(" takeTrackerNodeGroup cat not be null");
        }
        return getTableName();
    }

    private String getTableName() {
        return JobQueueUtils.getExecutableQueueName();
    }

    @Override
    public boolean add(TaskPo jobPo) {
        return super.add(getTableName(), jobPo);
    }

    @Override
    public boolean remove(String taskTrackerNodeGroup, String jobId) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .where("job_id = ?", jobId)
                .doDelete() == 1;
    }

    @Override
    public void resume(TaskPo jobPo) {

        new UpdateSql(getSqlTemplate())
                .update()
                .table(getTableName())
                .set("is_running", false)
                .set("task_tracker_identity", null)
                .set("gmt_modified", SystemClock.now())
                .where("task_id=?", jobPo.getTaskId())
                .doUpdate();
    }

    @Override
    public List<TaskPo> getDeadJob(String taskTrackerNodeGroup, long deadline) {

        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("is_running = ?", true)
                .and("gmt_modified < ?", deadline)
                .list(RshHolder.JOB_PO_LIST_RSH);
    }

    @Override
    public TaskPo getTask(String taskTrackerNodeGroup, String taskId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .and("task_tracker_node_group = ?", taskTrackerNodeGroup)
                .single(RshHolder.JOB_PO_RSH);
    }

}
