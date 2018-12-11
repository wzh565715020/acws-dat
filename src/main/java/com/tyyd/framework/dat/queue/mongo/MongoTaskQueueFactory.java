package com.tyyd.framework.dat.queue.mongo;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.queue.*;

public class MongoTaskQueueFactory implements TaskQueueFactory {

    @Override
    public TaskQueue getTaskQueue(Config config) {
        return new MongoTaskQueue(config);
    }

    @Override
    public ExecutableTaskQueue getExecutableJobQueue(Config config) {
        return new MongoExecutableTaskQueue(config);
    }

    @Override
    public ExecutingTaskQueue getExecutingJobQueue(Config config) {
        return new MongoExecutingTaskQueue(config);
    }

    @Override
    public TaskFeedbackQueue getTaskFeedbackQueue(Config config) {
        return new MongoTaskFeedbackQueue(config);
    }


    @Override
    public SuspendTaskQueue getSuspendTaskQueue(Config config) {
        return new MongoSuspendTaskQueue(config);
    }

    @Override
    public PreLoader getPreLoader(AppContext appContext) {
        return new MongoPreLoader(appContext);
    }
}
