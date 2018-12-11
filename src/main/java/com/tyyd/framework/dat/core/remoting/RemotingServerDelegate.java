package com.tyyd.framework.dat.core.remoting;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.exception.JobTrackerNotFoundException;
import com.tyyd.framework.dat.core.exception.RemotingSendException;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.RemotingServer;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;

import java.util.concurrent.ExecutorService;

/**
 *         对 remotingServer 的包装
 */
public class RemotingServerDelegate {

    private RemotingServer remotingServer;
    private AppContext appContext;
    // JobTracker 是否可用
    private volatile boolean serverEnable = false;
    
    public RemotingServerDelegate(RemotingServer remotingServer, AppContext appContext) {
        this.remotingServer = remotingServer;
        this.appContext = appContext;
    }

    public void start() {
        try {
            remotingServer.start();
        } catch (RemotingException e) {
            throw new RuntimeException(e);
        }
    }

    public void registerProcessor(int requestCode, RemotingProcessor processor,
                                  ExecutorService executor) {
        remotingServer.registerProcessor(requestCode, processor, executor);
    }

    public void registerDefaultProcessor(RemotingProcessor processor, ExecutorService executor) {
        remotingServer.registerDefaultProcessor(processor, executor);
    }

    public RemotingCommand invokeSync(Channel channel, RemotingCommand request)
            throws RemotingSendException {
        try {

            return remotingServer.invokeSync(channel, request,
                    appContext.getConfig().getInvokeTimeoutMillis());
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void invokeAsync(Channel channel, RemotingCommand request, AsyncCallback asyncCallback)
            throws RemotingSendException {
        try {

            remotingServer.invokeAsync(channel, request,
                    appContext.getConfig().getInvokeTimeoutMillis(), asyncCallback);
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }
    public void invokeOneway(Channel channel, RemotingCommand request)
            throws RemotingSendException {
        try {

            remotingServer.invokeOneway(channel, request,
                    appContext.getConfig().getInvokeTimeoutMillis());
        } catch (Throwable t) {
            throw new RemotingSendException(t);
        }
    }

    public void shutdown() {
        remotingServer.shutdown();
    }

	public boolean isServerEnable() {
		return serverEnable;
	}

	public void setServerEnable(boolean serverEnable) {
		this.serverEnable = serverEnable;
	}
    
}
