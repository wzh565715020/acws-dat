package com.tyyd.framework.dat.taskexecuter.domain;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelManager;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerFactory;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerPool;

public class TaskExecuterAppContext extends AppContext {

    private RemotingServerDelegate remotingServer;
    
    private RemotingClientDelegate remotingClient;
    
    // channel manager
    private ChannelManager channelManager;
    
    // runner 线程池
    private RunnerPool runnerPool;
    //
    private RunnerFactory runnerFactory;

    /**
     * 业务日志记录级别
     */
    private Level bizLogLevel;

    public RunnerPool getRunnerPool() {
        return runnerPool;
    }

    public void setRunnerPool(RunnerPool runnerPool) {
        this.runnerPool = runnerPool;
    }

    public Level getBizLogLevel() {
        return bizLogLevel;
    }

    public void setBizLogLevel(Level bizLogLevel) {
        this.bizLogLevel = bizLogLevel;
    }


    public RunnerFactory getRunnerFactory() {
        return runnerFactory;
    }

    public void setRunnerFactory(RunnerFactory runnerFactory) {
        this.runnerFactory = runnerFactory;
    }

    public RemotingServerDelegate getRemotingServer() {
		return remotingServer;
	}

	public void setRemotingServer(RemotingServerDelegate remotingServer) {
		this.remotingServer = remotingServer;
	}

	public ChannelManager getChannelManager() {
		return channelManager;
	}

	public void setChannelManager(ChannelManager channelManager) {
		this.channelManager = channelManager;
	}

	public RemotingClientDelegate getRemotingClient() {
		return remotingClient;
	}

	public void setRemotingClient(RemotingClientDelegate remotingClient) {
		this.remotingClient = remotingClient;
	}
}
