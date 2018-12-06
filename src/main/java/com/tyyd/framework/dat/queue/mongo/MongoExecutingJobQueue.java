package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.queue.ExecutingJobQueue;
import com.tyyd.framework.dat.queue.domain.JobPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class MongoExecutingJobQueue extends AbstractMongoJobQueue implements ExecutingJobQueue {

    public MongoExecutingJobQueue(Config config) {
        super(config);
        // table name (Collection name) for single table
        setTableName(JobQueueUtils.EXECUTING_JOB_QUEUE);

        // create table
        DBCollection dbCollection = template.getCollection();
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex("idx_jobId", "jobId", true, true);
            template.ensureIndex("idx_taskId_taskTrackerNodeGroup", "taskId, taskTrackerNodeGroup", true, true);
            template.ensureIndex("idx_taskTrackerIdentity", "taskTrackerIdentity");
            template.ensureIndex("idx_gmtCreated", "gmtCreated");
        }
    }

    @Override
    protected String getTargetTable(String taskTrackerNodeGroup) {
        return JobQueueUtils.EXECUTING_JOB_QUEUE;
    }

    @Override
    public boolean add(JobPo jobPo) {
        try {
            template.save(jobPo);
        } catch (DuplicateKeyException e) {
            // already exist
            throw new DupEntryException(e);
        }
        return true;
    }

    @Override
    public boolean remove(String jobId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("jobId").equal(jobId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    @Override
    public List<JobPo> getJobs(String taskTrackerIdentity) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("taskTrackerIdentity").equal(taskTrackerIdentity);
        return query.asList();
    }

    @Override
    public List<JobPo> getDeadJobs(long deadline) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.filter("gmtCreated < ", deadline);
        return query.asList();
    }

    @Override
    public JobPo getJob(String taskTrackerNodeGroup, String taskId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("taskId").equal(taskId).
                field("taskTrackerNodeGroup").equal(taskTrackerNodeGroup);
        return query.get();
    }

    @Override
    public JobPo getJob(String jobId) {
        Query<JobPo> query = template.createQuery(JobPo.class);
        query.field("jobId").equal(jobId);
        return query.get();
    }

}
