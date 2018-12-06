package com.tyyd.framework.dat.zookeeper.zkclient;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.zookeeper.ZkClient;
import com.tyyd.framework.dat.zookeeper.ZookeeperTransporter;

public class ZkClientZookeeperTransporter implements ZookeeperTransporter {

    public ZkClient connect(Config config) {
        return new ZkClientZkClient(config);
    }

}
