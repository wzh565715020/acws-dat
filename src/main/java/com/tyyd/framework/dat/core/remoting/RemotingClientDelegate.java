package com.tyyd.framework.dat.core.remoting;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.exception.TaskDispatcherNotFoundException;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingClient;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingConnectException;
import com.tyyd.framework.dat.remoting.exception.RemotingException;
import com.tyyd.framework.dat.remoting.exception.RemotingSendRequestException;
import com.tyyd.framework.dat.remoting.exception.RemotingTimeoutException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

import java.util.concurrent.ExecutorService;

public class RemotingClientDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(RemotingClientDelegate.class);

	private RemotingClient remotingClient;

	private AppContext appContext;

	public RemotingClientDelegate(RemotingClient remotingClient, AppContext appContext) {
		this.remotingClient = remotingClient;
		this.appContext = appContext;
	}

	public void start() {
		try {
			remotingClient.start();
		} catch (RemotingException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 异步调用
	 */
	public void invokeAsync(Channel channel, RemotingCommand request, AsyncCallback asyncCallback,
			String... addressParam) throws TaskDispatcherNotFoundException {
		try {
			if (channel != null) {
				remotingClient.invokeAsync(channel, request, appContext.getConfig().getInvokeTimeoutMillis(),
						asyncCallback);
			} else {
				String address = addressParam[0];
				remotingClient.invokeAsync(address, request, appContext.getConfig().getInvokeTimeoutMillis(),
						asyncCallback);
			}
		} catch (Throwable e) {
		}
	}

	public void registerProcessor(int requestCode, RemotingProcessor processor, ExecutorService executor) {
		remotingClient.registerProcessor(requestCode, processor, executor);
	}

	public void registerDefaultProcessor(RemotingProcessor processor, ExecutorService executor) {
		remotingClient.registerDefaultProcessor(processor, executor);
	}

	public void shutdown() {
		remotingClient.shutdown();
	}

	public RemotingClient getRemotingClient() {
		return remotingClient;
	}

	public void invokeAsync(String address, RemotingCommand request, AsyncCallback asyncCallback) throws Exception {
		remotingClient.invokeAsync(address, request, appContext.getConfig().getInvokeTimeoutMillis(), asyncCallback);
	}

	public void invokeAsync(TaskDispatcherAppContext appcontext, Node node, RemotingCommand request,
			AsyncCallback asyncCallback) throws Exception {
		remotingClient.invokeAsync(appcontext, node, request, appContext.getConfig().getInvokeTimeoutMillis(),
				asyncCallback);
	}

	/**
	 * 同步调用
	 * 
	 * @throws InterruptedException
	 * @throws RemotingTimeoutException
	 * @throws RemotingSendRequestException
	 * @throws RemotingConnectException
	 */
	public RemotingCommand invokeSync(String address, RemotingCommand request) throws RemotingConnectException,
			RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
		RemotingCommand response = remotingClient.invokeSync(address, request,
				appContext.getConfig().getInvokeTimeoutMillis());
		return response;
	}
}
