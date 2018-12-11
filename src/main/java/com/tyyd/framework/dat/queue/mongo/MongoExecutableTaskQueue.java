package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.util.List;

public class MongoExecutableTaskQueue extends AbstractMongoTaskQueue implements ExecutableTaskQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoExecutableTaskQueue.class);

    public MongoExecutableTaskQueue(Config config) {
        super(config);
    }

    @Override
    protected String getTargetTable() {
        return JobQueueUtils.getExecutableQueueName();
    }

    @Override
    public boolean add(TaskPo jobPo) {
        try {
            String tableName = JobQueueUtils.getExecutableQueueName();
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
        String tableName = JobQueueUtils.getExecutableQueueName();
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);
        query.field("jobId").equal(jobId);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    public void resume(TaskPo jobPo) {
        String tableName = JobQueueUtils.getExecutableQueueName();
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);

        query.field("taskId").equal(jobPo.getTaskId());

        UpdateOperations<TaskPo> operations =
                template.createUpdateOperations(TaskPo.class)
                        .set("isRunning", false)
                        .set("taskTrackerIdentity", "")
                        .set("gmtModified", SystemClock.now());
        template.update(query, operations);
    }

    @Override
    public List<TaskPo> getDeadJob(String taskTrackerNodeGroup, long deadline) {
        String tableName = JobQueueUtils.getExecutableQueueName();
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);
        query.field("isRunning").equal(true).
                filter("gmtCreated < ", deadline);
        return query.asList();
    }

    @Override
    public TaskPo getTask(String taskTrackerNodeGroup, String taskId) {
        String tableName = JobQueueUtils.getExecutableQueueName();
        Query<TaskPo> query = template.createQuery(tableName, TaskPo.class);
        query.field("taskId").equal(taskId).
                field("taskTrackerNodeGroup").equal(taskTrackerNodeGroup);
        return query.get();
    }
}
