package com.tyyd.framework.dat.biz.logger.mongo;


import com.tyyd.framework.dat.biz.logger.TaskLogger;
import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.JobLoggerRequest;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.store.mongo.MongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.mongodb.morphia.query.Query;

import java.util.Date;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class MongoJobLogger extends MongoRepository implements TaskLogger {

    public MongoJobLogger(Config config) {
        super(config);
        setTableName("lts_job_log_po");

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_logTime", "logTime");
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId,taskTrackerNodeGroup");
        }
    }

    @Override
    public void log(TaskLogPo jobLogPo) {
        template.save(jobLogPo);
    }

    @Override
    public void log(List<TaskLogPo> jobLogPos) {
        template.save(jobLogPos);
    }

    @Override
    public PaginationRsp<TaskLogPo> search(JobLoggerRequest request) {

        Query<TaskLogPo> query = template.createQuery(TaskLogPo.class);
        if(StringUtils.isNotEmpty(request.getTaskId())){
            query.field("taskId").equal(request.getTaskId());
        }
        if(StringUtils.isNotEmpty(request.getTaskTrackerNodeGroup())){
            query.field("taskTrackerNodeGroup").equal(request.getTaskTrackerNodeGroup());
        }
        if (request.getStartLogTime() != null) {
            query.filter("logTime >= ", getTimestamp(request.getStartLogTime()));
        }
        if (request.getEndLogTime() != null) {
            query.filter("logTime <= ", getTimestamp(request.getEndLogTime()));
        }
        PaginationRsp<TaskLogPo> paginationRsp = new PaginationRsp<TaskLogPo>();
        Long results = template.getCount(query);
        paginationRsp.setResults(results.intValue());
        if (results == 0) {
            return paginationRsp;
        }
        // 查询rows
        query.order("-logTime").offset(request.getStart()).limit(request.getLimit());

        paginationRsp.setRows(query.asList());

        return paginationRsp;
    }

    private Long getTimestamp(Date timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.getTime();
    }

}
