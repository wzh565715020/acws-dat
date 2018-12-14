package com.tyyd.framework.dat.core.cluster;

import com.tyyd.framework.dat.core.constant.Environment;

/**
 * 全局变量
 */
public class DATConfig {

    private static Environment environment = Environment.ONLINE;

    public static Environment getEnvironment() {
        if (environment == null) {
            return Environment.ONLINE;
        }
        return environment;
    }

    public static void setEnvironment(Environment environment) {
        DATConfig.environment = environment;
    }
}
