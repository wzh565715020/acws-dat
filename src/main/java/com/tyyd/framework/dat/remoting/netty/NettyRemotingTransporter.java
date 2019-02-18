package com.tyyd.framework.dat.remoting.netty;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.remoting.RemotingClient;
import com.tyyd.framework.dat.remoting.RemotingClientConfig;
import com.tyyd.framework.dat.remoting.RemotingServer;
import com.tyyd.framework.dat.remoting.RemotingServerConfig;
import com.tyyd.framework.dat.remoting.RemotingTransporter;

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
