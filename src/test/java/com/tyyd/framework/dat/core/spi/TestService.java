package com.tyyd.framework.dat.core.spi;

import com.tyyd.framework.dat.core.cluster.Config;

/**
 * @author Robert HG (254963746@qq.com) on 5/18/15.
 */
@SPI(key = "test.type", dftValue = "test2")
public interface TestService {

    public void sayHello(Config config);

}
