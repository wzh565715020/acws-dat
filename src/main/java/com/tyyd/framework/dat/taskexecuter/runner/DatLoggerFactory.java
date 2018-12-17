package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.taskexecuter.logger.BizLogger;

/**
 * 这个日志器将日志发送给DAT平台
 */
public final class DatLoggerFactory {

    private static final ThreadLocal<BizLogger> THREAD_LOCAL = new ThreadLocal<BizLogger>();

    public static BizLogger getBizLogger() {
        return THREAD_LOCAL.get();
    }

    protected static void setLogger(BizLogger logger){
        THREAD_LOCAL.set(logger);
    }

    protected static void remove(){
        THREAD_LOCAL.remove();
    }
}
