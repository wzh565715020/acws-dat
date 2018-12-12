package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.queue.TaskFeedbackQueue;
import com.tyyd.framework.dat.queue.domain.JobFeedbackPo;
import com.tyyd.framework.dat.store.mongo.MongoRepository;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import org.mongodb.morphia.query.Query;

import java.util.List;

/**
 * mongo 实现
 *
 */
public class MongoTaskFeedbackQueue extends MongoRepository implements TaskFeedbackQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoTaskFeedbackQueue.class);

    public MongoTaskFeedbackQueue(Config config) {
        super(config);
    }

    @Override
    public boolean add(List<JobFeedbackPo> jobFeedbackPos) {
        if (CollectionUtils.isEmpty(jobFeedbackPos)) {
            return true;
        }
        for (JobFeedbackPo jobFeedbackPo : jobFeedbackPos) {
            String tableName = TaskQueueUtils.getFeedbackQueueName();
            try {
                template.save(tableName, jobFeedbackPo);
            } catch (DuplicateKeyException e) {
                LOGGER.warn("duplicate key for job feedback po: " + JSON.toJSONString(jobFeedbackPo));
            }
        }
        return true;
    }

    @Override
    public boolean remove(String id) {
        Query<JobFeedbackPo> query = createQuery();
        query.field("id").equal(id);
        WriteResult wr = template.delete(query);
        return wr.getN() == 1;
    }

    private Query<JobFeedbackPo> createQuery() {
        String tableName = TaskQueueUtils.getFeedbackQueueName();
        return template.createQuery(tableName, JobFeedbackPo.class);
    }

    @Override
    public long getCount() {
        Query<JobFeedbackPo> query = createQuery();
        return template.getCount(query);
    }

    @Override
    public List<JobFeedbackPo> fetchTop(int top) {
        Query<JobFeedbackPo> query = createQuery();
        query.order("gmtCreated").limit(top);
        return query.asList();
    }
}
