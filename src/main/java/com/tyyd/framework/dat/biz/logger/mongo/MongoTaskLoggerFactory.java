package com.tyyd.framework.dat.biz.logger.mongo;

import com.tyyd.framework.dat.biz.logger.TaskLogger;
import com.tyyd.framework.dat.biz.logger.JobLoggerFactory;
import com.tyyd.framework.dat.core.cluster.Config;

public class MongoTaskLoggerFactory implements JobLoggerFactory {
    @Override
    public TaskLogger getJobLogger(Config config) {
        return new MongoJobLogger(config);
    }
}
