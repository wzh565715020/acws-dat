package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.admin.request.TaskQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.queue.TaskQueueInterface;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;
import com.tyyd.framework.dat.store.mongo.MongoRepository;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.Date;
import java.util.HashMap;

public abstract class AbstractMongoTaskQueue extends MongoRepository implements TaskQueueInterface {

    public AbstractMongoTaskQueue(Config config) {
        super(config);
    }

    @Override
    public PaginationRsp<TaskPo> pageSelect(TaskQueueReq request) {
        Query<TaskPo> query = template.createQuery(getTargetTable(), TaskPo.class);
        addCondition(query, "taskId", request.getTaskId());
        addCondition(query, "submitNode", request.getSubmitNode());
        if (request.getStartGmtCreated() != null) {
            query.filter("create_date >= ", request.getStartGmtCreated().getTime());
        }
        if (request.getEndGmtCreated() != null) {
            query.filter("create_date <= ", request.getEndGmtCreated().getTime());
        }
        if (request.getStartGmtModified() != null) {
            query.filter("update_date <= ", request.getStartGmtModified().getTime());
        }
        if (request.getEndGmtModified() != null) {
            query.filter("update_date >= ", request.getEndGmtModified().getTime());
        }
        PaginationRsp<TaskPo> response = new PaginationRsp<TaskPo>();
        Long results = template.getCount(query);
        response.setResults(results.intValue());
        if (results == 0) {
            return response;
        }

        if (StringUtils.isNotEmpty(request.getField()) && StringUtils.isNotEmpty(request.getDirection())) {
            query.order(("ASC".equalsIgnoreCase(request.getDirection()) ? "" : "-") + request.getField());
        }
        query.offset(request.getStart()).limit(request.getLimit());
        response.setRows(query.asList());
        return response;
    }

    @Override
    public boolean selectiveUpdate(TaskQueueReq request) {
        if (StringUtils.isEmpty(request.getTaskId())) {
            throw new JdbcException("Only allow by jobId");
        }
        Query<TaskPo> query = template.createQuery(getTargetTable(), TaskPo.class);
        query.field("jobId").equal(request.getTaskId());

        UpdateOperations<TaskPo> operations = template.createUpdateOperations(TaskPo.class);
        addUpdateField(operations, "cron", request.getCronExpression());
        addUpdateField(operations, "params", request.getExtParams());
        addUpdateField(operations, "triggerTime", request.getTriggerTime() == null ? null : request.getTriggerTime().getTime());
        addUpdateField(operations, "priority", request.getPriority());
        addUpdateField(operations, "maxRetryTimes", request.getMaxRetryTimes());
        addUpdateField(operations, "submitNode", request.getSubmitNode());
        addUpdateField(operations, "repeatCount", request.getRepeatCount());
        addUpdateField(operations, "repeatInterval", request.getRepeatInterval());

        UpdateResults ur = template.update(query, operations);
        return ur.getUpdatedCount() == 1;
    }

    private Query<TaskPo> addCondition(Query<TaskPo> query, String field, Object o) {
        if (!checkCondition(o)) {
            return query;
        }
        query.field(field).equal(o);
        return query;
    }

    private UpdateOperations<TaskPo> addUpdateField(UpdateOperations<TaskPo> operations, String field, Object o) {
        if (!checkCondition(o)) {
            return operations;
        }
        operations.set(field, o);
        return operations;
    }

    private boolean checkCondition(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof String) {
            if (StringUtils.isEmpty((String) obj)) {
                return false;
            }
        } else if (
                obj instanceof Integer ||
                obj instanceof Boolean ||
                        obj instanceof Long ||
                        obj instanceof Float ||
                        obj instanceof Date ||
                        obj instanceof HashMap) {
            return true;
        } else {
            throw new IllegalArgumentException("Can not support type " + obj.getClass());
        }

        return true;
    }

    protected abstract String getTargetTable();

}
