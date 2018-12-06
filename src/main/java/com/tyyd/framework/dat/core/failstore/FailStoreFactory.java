package com.tyyd.framework.dat.core.failstore;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

/**
 * Robert HG (254963746@qq.com) on 5/21/15.
 */
@SPI(key = SpiExtensionKey.FAIL_STORE, dftValue = "leveldb")
public interface FailStoreFactory {

    public FailStore getFailStore(Config config, String storePath);

}
