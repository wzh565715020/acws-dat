package com.tyyd.framework.dat.remoting.netty;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.remoting.*;

public class NettyRemotingTransporter implements RemotingTransporter {

    @Override
    public RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig) {
        return new NettyRemotingServer(appContext, remotingServerConfig);
    }

    @Override
    public RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig) {
        return new NettyRemotingClient(appContext, remotingClientConfig);
    }
}
