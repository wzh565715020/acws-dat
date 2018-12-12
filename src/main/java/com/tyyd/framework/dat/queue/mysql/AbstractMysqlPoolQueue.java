package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CharacterUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.queue.QueueInterface;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.JdbcAbstractAccess;
import com.tyyd.framework.dat.store.jdbc.builder.*;
import com.tyyd.framework.dat.store.jdbc.dbutils.JdbcTypeUtils;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;
import com.tyyd.framework.dat.admin.request.TaskQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

import java.util.List;

public abstract class AbstractMysqlPoolQueue extends JdbcAbstractAccess implements QueueInterface {

    public AbstractMysqlPoolQueue(Config config) {
        super(config);
    }

    protected boolean add(String tableName, PoolPo poolPo) {
        return new InsertSql(getSqlTemplate())
                .insert(tableName)
                .columns("pool_id",
                		"pool_name",
                		"max_count",
                		"task_ids",
                		"memo",
                        "create_date",
                        "update_date",
                        "create_userid",
                        "update_userid")
                .values(poolPo.getPoolId(),
                		poolPo.getPoolName(),
                		poolPo.getMaxCount(),
                		poolPo.getTaskIds(),
                		poolPo.getMemo(),
                		poolPo.getCreateDate(),
                		poolPo.getUpdateDate(),
                		poolPo.getCreateUserId(),
                		poolPo.getUpdateUserId())
                .doInsert() == 1;
    }

    public PaginationRsp<PoolPo> pageSelect(TaskQueueReq request) {

        PaginationRsp<PoolPo> response = new PaginationRsp<PoolPo>();

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

            List<PoolPo> poolPos = new SelectSql(getSqlTemplate())
                    .select()
                    .all()
                    .from()
                    .table(getTableName())
                    .whereSql(whereSql)
                    .orderBy()
                    .column(CharacterUtils.camelCase2Underscore(request.getField()), OrderByType.convert(request.getDirection()))
                    .limit(request.getStart(), request.getLimit())
                    .list(RshHolder.POOL_PO_LIST_RSH);
            response.setRows(poolPos);
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
                .setOnNotNull("cron", request.getCronExpression())
                .setOnNotNull("params", JSON.toJSONString(request.getExtParams()))
                .setOnNotNull("trigger_time", JdbcTypeUtils.toTimestamp(request.getTriggerTime()))
                .setOnNotNull("max_retry_times", request.getMaxRetryTimes())
                .setOnNotNull("submit_node", request.getSubmitNode())
                .setOnNotNull("repeat_count", request.getRepeatCount())
                .setOnNotNull("repeat_interval", request.getRepeatInterval())
                .where("task_id = ?", request.getTaskId())
                .doUpdate() == 1;
    }

    private WhereSql buildWhereSql(TaskQueueReq request) {
        return new WhereSql()
                .andOnNotEmpty("pool_id = ?", request.getTaskId())
                .andOnNotEmpty("submit_node = ?", request.getSubmitNode())
                .andBetween("create_date", JdbcTypeUtils.toTimestamp(request.getStartGmtCreated()), JdbcTypeUtils.toTimestamp(request.getEndGmtCreated()))
                .andBetween("update_date", JdbcTypeUtils.toTimestamp(request.getStartGmtModified()), JdbcTypeUtils.toTimestamp(request.getEndGmtModified()));
    }

}
