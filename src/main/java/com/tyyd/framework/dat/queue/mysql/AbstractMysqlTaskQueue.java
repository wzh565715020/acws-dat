package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.commons.utils.CharacterUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.queue.TaskQueueInterface;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.JdbcAbstractAccess;
import com.tyyd.framework.dat.store.jdbc.builder.InsertSql;
import com.tyyd.framework.dat.store.jdbc.builder.OrderByType;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;
import com.tyyd.framework.dat.store.jdbc.builder.WhereSql;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;
import com.tyyd.framework.dat.admin.request.TaskQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

import java.util.List;

public abstract class AbstractMysqlTaskQueue extends JdbcAbstractAccess implements TaskQueueInterface {

    protected boolean add(String tableName, TaskPo taskPo) {
        return new InsertSql(getSqlTemplate())
                .insert(tableName)
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
                        "repeat_interval")
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
                        taskPo.getRepeatInterval())
                .doInsert() == 1;
    }

    public PaginationRsp<TaskPo> pageSelect(TaskQueueReq request) {

        PaginationRsp<TaskPo> response = new PaginationRsp<TaskPo>();

        WhereSql whereSql = buildWhereSql(request);

        Long results = new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName())
                .whereSql(whereSql)
                .single();
        response.setResults(results.intValue());

        if (results > 0) {

            List<TaskPo> jobPos = new SelectSql(getSqlTemplate())
                    .select()
                    .all()
                    .from()
                    .table(getTableName())
                    .whereSql(whereSql)
                    .orderBy()
                    .column(CharacterUtils.camelCase2Underscore(request.getField()), OrderByType.convert(request.getDirection()))
                    .limit(request.getStart(), request.getLimit())
                    .list(RshHolder.TASK_PO_LIST_RSH);
            response.setRows(jobPos);
        }
        return response;
    }

    protected abstract String getTableName();

    public boolean selectiveUpdate(TaskQueueReq request) {

        if (StringUtils.isEmpty(request.getTaskId())) {
            throw new JdbcException("Only allow update by jobId");
        }
        return new UpdateSql(getSqlTemplate())
                .update()
                .table(getTableName())
                .setOnNotNull("cron", request.getCron())
                .setOnNotNull("trigger_time", request.getTriggerTime())
                .setOnNotNull("max_retry_times", request.getMaxRetryTimes())
                .setOnNotNull("submit_node", request.getSubmitNode())
                .setOnNotNull("repeat_count", request.getRepeatCount())
                .setOnNotNull("repeat_interval", request.getRepeatInterval())
                .where("task_id = ?", request.getTaskId())
                .doUpdate() == 1;
    }

    private WhereSql buildWhereSql(TaskQueueReq request) {
        return new WhereSql()
                .andOnNotEmpty("task_id = ?", request.getTaskId())
                .andOnNotEmpty("submit_node = ?", request.getSubmitNode())
                .andBetween("create_date", request.getCreateDate(), request.getCreateDate())
                .andBetween("update_date", request.getUpdateDate(), request.getUpdateDate());
    }

}
