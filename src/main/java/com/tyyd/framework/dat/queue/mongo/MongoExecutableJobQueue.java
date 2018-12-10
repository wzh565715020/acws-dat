package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.ExecutableJobQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class MongoExecutableJobQueue extends AbstractMongoJobQueue implements ExecutableJobQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoExecutableJobQueue.class);

    public MongoExecutableJobQueue(Config config) {
        super(config);
    }

    @Override
    protected String getTargetTable(String taskTrackerNodeGroup) {
        if (StringUtils.isEmpty(taskTrackerNodeGroup)) {
            throw new JdbcException("taskTrackerNodeGroup can not be null");
        }
        return JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
    }

    private ConcurrentHashSet<String> EXIST_TABLE = new ConcurrentHashSet<String>();

    @Override
    public boolean createQueue(String taskTrackerNodeGroup) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        DBCollection dbCollection = template.getCollection(tableName);
        List<DBObject> indexInfo = dbCollection.getIndexInfo();
        // create index if not exist
        if (CollectionUtils.sizeOf(indexInfo) <= 1) {
            template.ensureIndex(tableName, "idx_jobId", "jobId", true, true);
            template.ensureIndex(tableName, "idx_taskId_taskTrackerNodeGroup", "taskId, taskTrackerNodeGroup", true, true);
            template.ensureIndex(tableName, "idx_taskTrackerIdentity", "taskTrackerIdentity");
            template.ensureIndex(tableName, "idx_triggerTime_priority_gmtCreated", "triggerTime,priority,gmtCreated");
            template.ensureIndex(tableName, "idx_isRunning", "isRunning");
            LOGGER.info("create queue " + tableName);
        }
        EXIST_TABLE.add(tableName);
        return true;
    }

    @Override
    public boolean removeQueue(String taskTrackerNodeGroup) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        DBCollection dbCollection = template.getCollection(tableName);
        dbCollection.drop();
        LOGGER.info("drop queue " + tableName);

        return true;
    }

    @Override
    public boolean add(TaskPo jobPo) {
        try {
            String tableName = JobQueueUtils.getExecutableQueueName(jobPo.getTaskTrackerNodeGroup());
            if (!EXIST_TABLE.contains(tableName)) {
                createQueue(jobPo.getTaskTrackerNodeGroup());
            }
            jobPo.setGmtCreated(SystemClock.now());
            jobPo.setGmtModified(jobPo.getGmtCreated());
            template.save(tableName, jobPo);
        } catch (DuplicateKeyException e) {
            // 已经存在
            throw new DupEntryException(e);
        }
        return true;
    }

    @Override
    public boolean remove(String taskTrackerNodeGroup, String jobId) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);
        query.field("jobId").equal(jobId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    public void resume(TaskPo jobPo) {
        String tableName = JobQueueUtils.getExecutableQueueName(jobPo.getTaskTrackerNodeGroup());
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);

        query.field("jobId").equal(jobPo.getJobId());

        UpdateOperations<TaskPo> operations =
                template.createUpdateOperations(TaskPo.class)
                        .set("isRunning", false)
                        .set("taskTrackerIdentity", "")
                        .set("gmtModified", SystemClock.now());
        template.update(query, operations);
    }

    @Override
    public List<TaskPo> getDeadJob(String taskTrackerNodeGroup, long deadline) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);
        query.field("isRunning").equal(true).
                filter("gmtCreated < ", deadline);
        return query.asList();
    }

    @Override
    public TaskPo getJob(String taskTrackerNodeGroup, String taskId) {
        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);
        query.field("taskId").equal(taskId).
                field("taskTrackerNodeGroup").equal(taskTrackerNodeGroup);
        return query.get();
    }
}
