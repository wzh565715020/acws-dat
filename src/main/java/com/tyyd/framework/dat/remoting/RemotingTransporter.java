package com.tyyd.framework.dat.remoting;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

@SPI(key = SpiExtensionKey.REMOTING, dftValue = "netty")
public interface RemotingTransporter {

    RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig);

    RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig);

}
