package com.tyyd.framework.dat.remoting.mina;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.remoting.*;

/**
 * @author Robert HG (254963746@qq.com) on 11/6/15.
 */
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
