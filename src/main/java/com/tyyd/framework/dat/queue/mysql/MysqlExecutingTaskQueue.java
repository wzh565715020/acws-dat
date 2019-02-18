package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;

import java.util.List;

public class MysqlExecutingTaskQueue extends AbstractMysqlTaskExecuteQueue implements ExecutingTaskQueue {

    @Override
    public boolean add(TaskPo taskPo) {
        return super.add(getTableName(), taskPo);
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
    public List<TaskPo> getTasks(String taskTrackerIdentity) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_execute_node = ?", taskTrackerIdentity)
                .list(RshHolder.TASK_PO_LIST_RSH);
    }

    @Override
    public List<TaskPo> getDeadTasks(long deadline) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("create_date < ?", deadline)
                .list(RshHolder.TASK_PO_LIST_RSH);
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

    @Override
    public TaskPo getTask(String id) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("id = ?", id)
                .single(RshHolder.TASK_PO_RSH);
    }
    @Override
    protected String getTableName() {
        return TaskQueueUtils.EXECUTING_TASK_QUEUE;
    }

}
