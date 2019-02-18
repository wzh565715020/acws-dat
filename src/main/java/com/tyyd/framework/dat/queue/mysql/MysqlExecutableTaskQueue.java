package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;

import java.util.List;

public class MysqlExecutableTaskQueue extends AbstractMysqlTaskExecuteQueue implements ExecutableTaskQueue {

    @Override
    protected String getTableName() {
        return TaskQueueUtils.getExecutableQueueName();
    }

    @Override
    public boolean add(TaskPo jobPo) {
        return super.add(getTableName(), jobPo);
    }

    @Override
    public boolean remove(String id) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .where("id = ?", id)
                .doDelete() == 1;
    }

    @Override
    public void resume(TaskPo taskPo) {

        new UpdateSql(getSqlTemplate())
                .update()
                .table(getTableName())
                .set("is_running", false)
                .set("task_execute_node", null)
                .set("update_date", SystemClock.now())
                .where("id = ?", taskPo.getId())
                .doUpdate();
    }

    @Override
    public List<TaskPo> getDeadJob(long deadline) {

        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("is_running = ?", true)
                .and("update_date < ?", deadline)
                .list(RshHolder.TASK_PO_LIST_RSH);
    }

    @Override
    public TaskPo getTask(String taskId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("id = ?", taskId)
                .single(RshHolder.TASK_PO_RSH);
    }

	@Override
	public List<TaskPo> getTaskByTaskId(String taskId) {
		return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .list(RshHolder.TASK_PO_LIST_RSH);
	}

}
