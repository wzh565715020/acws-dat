package com.tyyd.framework.dat.core.remoting;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.exception.TaskDispatcherNotFoundException;
import com.tyyd.framework.dat.core.loadbalance.LoadBalance;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.ec.EventInfo;
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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class RemotingClientDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(RemotingClientDelegate.class);

	private RemotingClient remotingClient;

	private AppContext appContext;

	private List<Node> taskExecuters;

	public RemotingClientDelegate(RemotingClient remotingClient, AppContext appContext) {
		this.remotingClient = remotingClient;
		this.appContext = appContext;
		this.taskExecuters = new CopyOnWriteArrayList<Node>();
	}

	public Node getTaskExecuterNode() {
		if (taskExecuters.size() == 0) {
			EventInfo eventInfo = new EventInfo(EcTopic.NO_TASK_EXECUTER_AVAILABLE);
			appContext.getEventCenter().publishAsync(eventInfo);
			return null;
		}
		// 连taskExecuters的负载均衡算法
		LoadBalance loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig());
		return loadBalance.select(taskExecuters, appContext.getConfig().getIdentity());
	}

	public void start() {
		try {
			remotingClient.start();
		} catch (RemotingException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean contains(Node taskExecuter) {
		return taskExecuters.contains(taskExecuter);
	}

	public void addTaskExecuter(Node taskExecuter) {
		if (!contains(taskExecuter)) {
			taskExecuters.add(taskExecuter);
		}
	}

	public boolean removeTaskExecuter(Node taskExecuter) {
		return taskExecuters.remove(taskExecuter);
	}

	/**
	 * 同步调用
	 */
	public RemotingCommand invokeSync(RemotingCommand request) throws TaskDispatcherNotFoundException {

		Node taskExecuter = getTaskExecuterNode();

		try {
			RemotingCommand response = remotingClient.invokeSync(taskExecuter.getAddress(), request,
					appContext.getConfig().getInvokeTimeoutMillis());
			return response;
		} catch (Exception e) {
			// 将这个JobTracker移除
			taskExecuters.remove(taskExecuter);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			// 只要不是节点 不可用, 轮询所有节点请求
			return invokeSync(request);
		}
	}

	/**
	 * 异步调用
	 */
	public void invokeAsync(RemotingCommand request, AsyncCallback asyncCallback)
			throws TaskDispatcherNotFoundException {

		Node taskExecuter = getTaskExecuterNode();

		try {
			remotingClient.invokeAsync(taskExecuter.getAddress(), request,
					appContext.getConfig().getInvokeTimeoutMillis(), asyncCallback);
		} catch (Throwable e) {
			// 将这个JobTracker移除
			taskExecuters.remove(taskExecuter);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			// 只要不是节点 不可用, 轮询所有节点请求
			invokeAsync(request, asyncCallback);
		}
	}

	/**
	 * 异步调用
	 */
	public void invokeAsync(Channel channel, RemotingCommand request, AsyncCallback asyncCallback)
			throws TaskDispatcherNotFoundException {
		try {
			remotingClient.invokeAsync(channel, request, appContext.getConfig().getInvokeTimeoutMillis(),
					asyncCallback);
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
		try {
			remotingClient.invokeAsync(appcontext, node, request, appContext.getConfig().getInvokeTimeoutMillis(),
					asyncCallback);
		} catch (Exception e) {
			// 将这个taskExecuter移除
			taskExecuters.remove(node);
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e1) {
				LOGGER.error(e1.getMessage(), e1);
			}
			// 只要不是节点 不可用, 轮询所有节点请求
			invokeAsync(appcontext,getTaskExecuterNode(),request, asyncCallback);
		}
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
