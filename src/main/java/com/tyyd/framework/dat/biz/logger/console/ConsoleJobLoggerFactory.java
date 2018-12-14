package com.tyyd.framework.dat.biz.logger.console;

import com.tyyd.framework.dat.biz.logger.TaskLogger;
import com.tyyd.framework.dat.biz.logger.JobLoggerFactory;
import com.tyyd.framework.dat.core.cluster.Config;

/**
 * @author   on 12/27/15.
 */
public class ConsoleJobLoggerFactory implements JobLoggerFactory {
    @Override
    public TaskLogger getJobLogger(Config config) {
        return new ConsoleJobLogger();
    }
}
