package com.tyyd.framework.dat.zookeeper.lts;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.zookeeper.ZkClient;
import com.tyyd.framework.dat.zookeeper.ZookeeperTransporter;

public class LtsZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new LtsZkClient(config);
    }

}
