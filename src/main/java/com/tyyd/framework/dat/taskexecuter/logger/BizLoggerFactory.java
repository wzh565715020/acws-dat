package com.tyyd.framework.dat.taskexecuter.logger;


import java.util.concurrent.ConcurrentHashMap;

import com.tyyd.framework.dat.core.cluster.LTSConfig;
import com.tyyd.framework.dat.core.constant.Environment;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

public class BizLoggerFactory {

    private static final ConcurrentHashMap<String, BizLogger> BIZ_LOGGER_CONCURRENT_HASH_MAP = new ConcurrentHashMap<String, BizLogger>();

    /**
     * 保证一个TaskTracker只能有一个Logger, 因为一个jvm可以有多个TaskTracker
     */
    public static BizLogger getLogger(Level level, RemotingServerDelegate remotingServer, TaskExecuterAppContext appContext) {

        // 单元测试的时候返回 Mock
        if (Environment.UNIT_TEST == LTSConfig.getEnvironment()) {
            return new MockBizLogger(level);
        }

        String key = appContext.getConfig().getIdentity();
        BizLogger logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
        if (logger == null) {
            synchronized (BIZ_LOGGER_CONCURRENT_HASH_MAP) {
                logger = BIZ_LOGGER_CONCURRENT_HASH_MAP.get(key);
                if (logger != null) {
                    return logger;
                }
               // logger = new BizLoggerImpl(level, remotingServer, appContext);

                BIZ_LOGGER_CONCURRENT_HASH_MAP.put(key, logger);
            }
        }
        return logger;
    }

}
