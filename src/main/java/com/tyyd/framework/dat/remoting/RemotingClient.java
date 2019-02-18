package com.tyyd.framework.dat.remoting;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.remoting.exception.RemotingConnectException;
import com.tyyd.framework.dat.remoting.exception.RemotingException;
import com.tyyd.framework.dat.remoting.exception.RemotingSendRequestException;
import com.tyyd.framework.dat.remoting.exception.RemotingTimeoutException;
import com.tyyd.framework.dat.remoting.exception.RemotingTooMuchRequestException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

import java.util.concurrent.ExecutorService;

/**
 * 远程通信，Client接口
 */
public interface RemotingClient {

	void start() throws RemotingException;

	/**
	 * 同步调用
	 */
	RemotingCommand invokeSync(final String addr, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingSendRequestException,
			RemotingTimeoutException;

	/**
	 * 异步调用
	 */
	void invokeAsync(final String addr, final RemotingCommand request, final long timeoutMillis,
			final AsyncCallback asyncCallback) throws InterruptedException, RemotingConnectException,
			RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

	/**
	 * 单向调用
	 */
	void invokeOneway(final String addr, final RemotingCommand request, final long timeoutMillis)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException;

	/**
	 * 注册处理器
	 */
	void registerProcessor(final int requestCode, final RemotingProcessor processor, final ExecutorService executor);

	/**
	 * 注册默认处理器
	 */
	void registerDefaultProcessor(final RemotingProcessor processor, final ExecutorService executor);

	void shutdown();

	Channel getAndCreateChannel(String addr) throws InterruptedException;

	void invokeAsync(Channel channel, RemotingCommand request, long timeoutMillis, AsyncCallback asyncCallback)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException;


	void invokeAsync(TaskDispatcherAppContext appcontext,Node node, RemotingCommand request, long timeoutMillis, AsyncCallback asyncCallback)
			throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
			RemotingTimeoutException, RemotingSendRequestException;
}
