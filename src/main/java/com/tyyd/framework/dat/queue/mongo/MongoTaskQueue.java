package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.TaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.List;

public class MongoTaskQueue extends AbstractMongoTaskQueue implements TaskQueue {

    public MongoTaskQueue(Config config) {
        super(config);
        // table name (Collection name) for single table
        setTableName(TaskQueueUtils.TASK_QUEUE);

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
    protected String getTargetTable() {
        return TaskQueueUtils.TASK_QUEUE;
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
    public TaskPo getTask(String taskId) {
        Query<TaskPo> query = template.createQuery(TaskPo.class);
        query.field("task_id").equal(taskId);
        return query.get();
    }

    @Override
    public boolean remove(String taskId) {
        Query<TaskPo> query = template.createQuery(TaskPo.class);
        query.field("jobId").equal(taskId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    @Override
    public int incRepeatedCount(String jobId) {
        while (true) {
            TaskPo jobPo = getTask(jobId);
            if (jobPo == null) {
                return -1;
            }
            Query<TaskPo> query = template.createQuery(TaskPo.class);
            query.field("jobId").equal(jobId);
            query.field("repeatedCount").equal(jobPo.getRepeatedCount());

            UpdateOperations<TaskPo> opts = template.createUpdateOperations(TaskPo.class);
            opts.set("repeatedCount", jobPo.getRepeatedCount() + 1);

            UpdateResults ur = template.update(query, opts);
            if (ur.getUpdatedCount() == 1) {
                return jobPo.getRepeatedCount() + 1;
            }
        }
    }
}
