package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CharacterUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.queue.TaskQueueInterface;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.JdbcAbstractAccess;
import com.tyyd.framework.dat.store.jdbc.builder.*;
import com.tyyd.framework.dat.store.jdbc.dbutils.JdbcTypeUtils;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;
import com.tyyd.framework.dat.admin.request.JobQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/31/15.
 */
public abstract class AbstractMysqlTaskQueue extends JdbcAbstractAccess implements TaskQueueInterface {

    public AbstractMysqlTaskQueue(Config config) {
        super(config);
    }

    protected boolean add(String tableName, TaskPo jobPo) {
        return new InsertSql(getSqlTemplate())
                .insert(tableName)
                .columns("task_id",
                        "priority",
                        "retry_times",
                        "max_retry_times",
                        "gmt_created",
                        "gmt_modified",
                        "submit_node_group",
                        "task_tracker_node_group",
                        "ext_params",
                        "internal_ext_params",
                        "is_running",
                        "task_tracker_identity",
                        "need_feedback",
                        "cron_expression",
                        "trigger_time",
                        "repeat_count",
                        "repeated_count",
                        "repeat_interval")
                .values(jobPo.getTaskId(),
                        jobPo.getPriority(),
                        jobPo.getRetryTimes(),
                        jobPo.getMaxRetryTimes(),
                        jobPo.getGmtCreated(),
                        jobPo.getGmtModified(),
                        jobPo.getSubmitNodeGroup(),
                        jobPo.getTaskTrackerNodeGroup(),
                        JSON.toJSONString(jobPo.getExtParams()),
                        JSON.toJSONString(jobPo.getInternalExtParams()),
                        jobPo.isRunning(),
                        jobPo.getTaskTrackerIdentity(),
                        jobPo.isNeedFeedback(),
                        jobPo.getCronExpression(),
                        jobPo.getTriggerTime(),
                        jobPo.getRepeatCount(),
                        jobPo.getRepeatedCount(),
                        jobPo.getRepeatInterval())
                .doInsert() == 1;
    }

    public PaginationRsp<TaskPo> pageSelect(JobQueueReq request) {

        PaginationRsp<TaskPo> response = new PaginationRsp<TaskPo>();

        WhereSql whereSql = buildWhereSql(request);

        Long results = new SelectSql(getSqlTemplate())
                .select()
                .columns("count(1)")
                .from()
                .table(getTableName(request))
                .whereSql(whereSql)
                .single();
        response.setResults(results.intValue());

        if (results > 0) {

            List<TaskPo> jobPos = new SelectSql(getSqlTemplate())
                    .select()
                    .all()
                    .from()
                    .table(getTableName(request))
                    .whereSql(whereSql)
                    .orderBy()
                    .column(CharacterUtils.camelCase2Underscore(request.getField()), OrderByType.convert(request.getDirection()))
                    .limit(request.getStart(), request.getLimit())
                    .list(RshHolder.JOB_PO_LIST_RSH);
            response.setRows(jobPos);
        }
        return response;
    }

    protected abstract String getTableName(JobQueueReq request);

    public boolean selectiveUpdate(JobQueueReq request) {

        if (StringUtils.isEmpty(request.getJobId())) {
            throw new JdbcException("Only allow update by jobId");
        }
        return new UpdateSql(getSqlTemplate())
                .update()
                .table(getTableName(request))
                .setOnNotNull("cron_expression", request.getCronExpression())
                .setOnNotNull("need_feedback", request.getNeedFeedback())
                .setOnNotNull("ext_params", JSON.toJSONString(request.getExtParams()))
                .setOnNotNull("trigger_time", JdbcTypeUtils.toTimestamp(request.getTriggerTime()))
                .setOnNotNull("priority", request.getPriority())
                .setOnNotNull("max_retry_times", request.getMaxRetryTimes())
                .setOnNotNull("submit_node_group", request.getSubmitNodeGroup())
                .setOnNotNull("task_tracker_node_group", request.getTaskTrackerNodeGroup())
                .setOnNotNull("repeat_count", request.getRepeatCount())
                .setOnNotNull("repeat_interval", request.getRepeatInterval())
                .where("job_id=?", request.getJobId())
                .doUpdate() == 1;
    }

    private WhereSql buildWhereSql(JobQueueReq request) {
        return new WhereSql()
                .andOnNotEmpty("job_id = ?", request.getJobId())
                .andOnNotEmpty("task_id = ?", request.getTaskId())
                .andOnNotEmpty("task_tracker_node_group = ?", request.getTaskTrackerNodeGroup())
                .andOnNotEmpty("submit_node_group = ?", request.getSubmitNodeGroup())
                .andOnNotNull("need_feedback = ?", request.getNeedFeedback())
                .andBetween("gmt_created", JdbcTypeUtils.toTimestamp(request.getStartGmtCreated()), JdbcTypeUtils.toTimestamp(request.getEndGmtCreated()))
                .andBetween("gmt_modified", JdbcTypeUtils.toTimestamp(request.getStartGmtModified()), JdbcTypeUtils.toTimestamp(request.getEndGmtModified()));
    }

}
