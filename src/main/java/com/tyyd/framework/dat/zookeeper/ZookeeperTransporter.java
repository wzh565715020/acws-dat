package com.tyyd.framework.dat.zookeeper;


import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

@SPI(key = SpiExtensionKey.ZK_CLIENT_KEY, dftValue = "zkclient")
public interface ZookeeperTransporter {

    ZkClient connect(Config config);

}
