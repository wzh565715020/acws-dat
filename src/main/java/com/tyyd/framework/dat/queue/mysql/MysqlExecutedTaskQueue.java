package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.ExecutedTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.InsertSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;

import java.util.List;

public class MysqlExecutedTaskQueue extends AbstractMysqlTaskExecuteQueue implements ExecutedTaskQueue {

    public MysqlExecutedTaskQueue() {
        super();
    }

    @Override
    public boolean add(TaskPo taskPo) {
    	return new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("id","task_id",
                		"task_name",
                		"task_class",
                		"task_type",
                		"task_exec_type",
                        "retry_times",
                        "max_retry_times",
                        "create_date",
                        "update_date",
                        "submit_node",
                        "params",
                        "cron",
                        "trigger_time",
                        "repeat_count",
                        "repeated_count",
                        "repeat_interval",
                        "task_execute_node",
                        "pool_id")
                .values(taskPo.getId(),
                		taskPo.getTaskId(),
                		taskPo.getTaskName(),
                		taskPo.getTaskClass(),
                		taskPo.getTaskType(),
                		taskPo.getTaskExecType(),
                        taskPo.getRetryTimes(),
                        taskPo.getMaxRetryTimes(),
                        taskPo.getCreateDate(),
                        taskPo.getUpdateDate(),
                        taskPo.getSubmitNode(),
                        taskPo.getParams(),
                        taskPo.getCron(),
                        taskPo.getTriggerTime(),
                        taskPo.getRepeatCount(),
                        taskPo.getRepeatedCount(),
                        taskPo.getRepeatInterval(),
                        taskPo.getTaskExecuteNode(),
                        taskPo.getPoolId())
                .doInsert() == 1;
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
    public TaskPo getTask(String taskTrackerNode, String taskId) {
        return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("task_id = ?", taskId)
                .and("task_execute_node = ?", taskTrackerNode)
                .single(RshHolder.TASK_PO_RSH);
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
    protected String getTableName() {
        return TaskQueueUtils.EXECUTED_TASK_QUEUE;
    }
}
