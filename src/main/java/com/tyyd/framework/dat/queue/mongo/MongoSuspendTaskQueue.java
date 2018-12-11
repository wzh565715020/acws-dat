package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.queue.SuspendTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author bug (357693306@qq.com) on 3/4/16.
 */
public class MongoSuspendTaskQueue extends AbstractMongoTaskQueue implements SuspendTaskQueue {

    public MongoSuspendTaskQueue(Config config) {
        super(config);
        // table name (Collection name) for single table
        setTableName(JobQueueUtils.SUSPEND_JOB_QUEUE);

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_jobId", "jobId", true, true);
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId, taskTrackerNodeGroup", true, true);
        }
    }

    @Override
    protected String getTargetTable(){
        return JobQueueUtils.SUSPEND_JOB_QUEUE;
    }

    @Override
    public boolean add(TaskPo jobPo) {
        try {
            template.save(jobPo);
        } catch (DuplicateKeyException e) {
            // 已经存在
            throw new DupEntryException(e);
        }
        return true;
    }

    @Override
    public TaskPo getJob(String jobId) {
        Query<TaskPo> query = template.createQuery(TaskPo.class);
        query.field("jobId").equal(jobId);
        return query.get();
    }

    @Override
    public boolean remove(String jobId) {
        Query<TaskPo> query = template.createQuery(TaskPo.class);
        query.field("jobId").equal(jobId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

}
