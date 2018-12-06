package com.tyyd.framework.dat.taskexecuter.logger;

import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;

public class MockBizLogger extends BizLoggerAdapter implements BizLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockBizLogger.class);
    private Level level;

    public MockBizLogger(Level level) {
        this.level = level;
        if (level == null) {
            this.level = Level.INFO;
        }
    }

    @Override
    public void debug(String msg) {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            LOGGER.debug(msg);
        }
    }

    @Override
    public void info(String msg) {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            LOGGER.info(msg);
        }
    }

    @Override
    public void error(String msg) {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            LOGGER.error(msg);
        }
    }

    @Override
    public void setId(String jobId, String taskId) {

    }

    @Override
    public void removeId() {

    }
}
