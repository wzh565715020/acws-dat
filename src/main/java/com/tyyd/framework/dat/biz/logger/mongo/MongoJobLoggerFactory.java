package com.tyyd.framework.dat.biz.logger.mongo;

import com.tyyd.framework.dat.biz.logger.JobLogger;
import com.tyyd.framework.dat.biz.logger.JobLoggerFactory;
import com.tyyd.framework.dat.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 12/27/15.
 */
public class MongoJobLoggerFactory implements JobLoggerFactory {
    @Override
    public JobLogger getJobLogger(Config config) {
        return new MongoJobLogger(config);
    }
}
