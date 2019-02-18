package com.tyyd.framework.dat.remoting.mina;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.remoting.RemotingClient;
import com.tyyd.framework.dat.remoting.RemotingClientConfig;
import com.tyyd.framework.dat.remoting.RemotingServer;
import com.tyyd.framework.dat.remoting.RemotingServerConfig;
import com.tyyd.framework.dat.remoting.RemotingTransporter;

public class MinaRemotingTransporter implements RemotingTransporter {
    @Override
    public RemotingServer getRemotingServer(AppContext appContext, RemotingServerConfig remotingServerConfig) {
        return new MinaRemotingServer(remotingServerConfig);
    }

    @Override
    public RemotingClient getRemotingClient(AppContext appContext, RemotingClientConfig remotingClientConfig) {
        return new MinaRemotingClient(remotingClientConfig);
    }
}
