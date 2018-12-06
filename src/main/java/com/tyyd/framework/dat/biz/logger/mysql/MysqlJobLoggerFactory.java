package com.tyyd.framework.dat.biz.logger.mysql;

import com.tyyd.framework.dat.biz.logger.JobLogger;
import com.tyyd.framework.dat.biz.logger.JobLoggerFactory;
import com.tyyd.framework.dat.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 12/27/15.
 */
public class MysqlJobLoggerFactory implements JobLoggerFactory {
    @Override
    public JobLogger getJobLogger(Config config) {
        return new MysqlJobLogger(config);
    }
}
