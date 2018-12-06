package com.tyyd.framework.dat.remoting;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
@SPI(key = SpiExtensionKey.REMOTING, dftValue = "netty")
public interface RemotingTransporter {

    RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig);

    RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig);

}
