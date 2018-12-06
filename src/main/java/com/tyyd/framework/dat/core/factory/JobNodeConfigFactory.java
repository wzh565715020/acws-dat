package com.tyyd.framework.dat.core.factory;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.constant.Constants;


public class JobNodeConfigFactory {

    public static Config getDefaultConfig() {
        Config config = new Config();
        config.setIdentity(StringUtils.generateUUID());
        config.setWorkThreads(Constants.AVAILABLE_PROCESSOR);
        config.setNodeGroup("lts");
        config.setRegistryAddress("zookeeper://127.0.0.1:2181");
        config.setInvokeTimeoutMillis(1000 * 60);
//        config.setListenPort(Constants.JOB_TRACKER_DEFAULT_LISTEN_PORT);
        config.setDataPath(Constants.USER_HOME);
        config.setClusterName(Constants.DEFAULT_CLUSTER_NAME);
        return config;
    }

}
