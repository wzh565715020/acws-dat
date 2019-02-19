package com.tyyd.framework.dat.core.commons.utils;


import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;

public class QuietUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuietUtils.class);

    public static void doWithError(Callable callable) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void doWithWarn(Callable callable) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
    }

    public static void doWithInfo(Callable callable) {
        try {
            callable.call();
        } catch (Exception e) {
            LOGGER.info(e.getMessage(), e);
        }
    }

    public static void doQuietly(Callable callable) {
        try {
            callable.call();
        } catch (Exception ignored) {
        }
    }

    public static void sleep(long millis){
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

}
