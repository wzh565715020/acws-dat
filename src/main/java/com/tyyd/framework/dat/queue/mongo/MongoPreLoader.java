package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.support.JobQueueUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.AbstractPreLoader;
import com.tyyd.framework.dat.queue.domain.JobPo;
import com.tyyd.framework.dat.store.mongo.DataStoreProvider;
import com.tyyd.framework.dat.store.mongo.MongoTemplate;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 8/13/15.
 */
public class MongoPreLoader extends AbstractPreLoader {

    private MongoTemplate template;

    public MongoPreLoader(final AppContext appContext) {
        super(appContext);
        this.template = new MongoTemplate(
                (AdvancedDatastore) DataStoreProvider.getDataStore(appContext.getConfig()));
    }

    protected boolean lockJob(String taskTrackerNodeGroup, String jobId, String taskTrackerIdentity, Long triggerTime, Long gmtModified) {
        UpdateOperations<JobPo> operations =
                template.createUpdateOperations(JobPo.class)
                        .set("isRunning", true)
                        .set("taskTrackerIdentity", taskTrackerIdentity)
                        .set("gmtModified", SystemClock.now());

        String tableName = JobQueueUtils.getExecutableQueueName(taskTrackerNodeGroup);

        Query<JobPo> updateQuery = template.createQuery(tableName, JobPo.class);
        updateQuery.field("jobId").equal(jobId)
                .field("isRunning").equal(false)
                .field("triggerTime").equal(triggerTime)
                .field("gmtModified").equal(gmtModified);
        UpdateResults updateResult = template.update(updateQuery, operations);
        return updateResult.getUpdatedCount() == 1;
    }

    protected List<JobPo> load(String loadTaskTrackerNodeGroup, int loadSize) {
        // load
        String tableName = JobQueueUtils.getExecutableQueueName(loadTaskTrackerNodeGroup);
        Query<JobPo> query = template.createQuery(tableName, JobPo.class);
        query.field("isRunning").equal(false)
                .filter("triggerTime < ", SystemClock.now())
                .order(" triggerTime, priority , gmtCreated").offset(0).limit(loadSize);
        return query.asList();
    }

}
