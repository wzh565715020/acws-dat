package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.TaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;

public class MysqlTaskQueue extends AbstractMysqlTaskQueue implements TaskQueue {

    @Override
    public boolean add(TaskPo taskPo) {
        return super.add(getTableName(), taskPo);
    }

    @Override
    public TaskPo getTask(String taskId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .single(RshHolder.TASK_PO_RSH);
    }

    @Override
    public boolean remove(String taskId) {
        return new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .doDelete() == 1;
    }


    @Override
    public int incRepeatedCount(String taskId) {
        while (true) {
            TaskPo taskPo = getTask(taskId);
            if (taskPo == null) {
                return -1;
            }
            if (new UpdateSql(getSqlTemplate())
                    .update()
                    .table(getTableName())
                    .set("repeated_count", taskPo.getRepeatedCount() + 1)
                    .where("task_id = ?", taskId)
                    .and("repeated_count = ?", taskPo.getRepeatedCount())
                    .doUpdate() == 1) {
                return taskPo.getRepeatedCount() + 1;
            }
        }
    }

    @Override
    protected String getTableName() {
        return TaskQueueUtils.TASK_QUEUE;
    }

}
