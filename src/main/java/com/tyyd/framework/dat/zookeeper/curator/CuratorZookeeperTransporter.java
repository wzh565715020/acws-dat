package com.tyyd.framework.dat.zookeeper.curator;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.zookeeper.ZkClient;
import com.tyyd.framework.dat.zookeeper.ZookeeperTransporter;

public class CuratorZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new CuratorZkClient(config);
    }

}