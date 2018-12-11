package com.tyyd.framework.dat.core.remoting;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.exception.JobTrackerNotFoundException;
import com.tyyd.framework.dat.core.loadbalance.LoadBalance;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.ec.EventInfo;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingClient;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

public class RemotingClientDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingClientDelegate.class);

    private RemotingClient remotingClient;
    private AppContext appContext;

    // JobTracker 是否可用
    private volatile boolean serverEnable = false;
    private List<Node> taskExecuters;

    public RemotingClientDelegate(RemotingClient remotingClient, AppContext appContext) {
        this.remotingClient = remotingClient;
        this.appContext = appContext;
        this.taskExecuters = new CopyOnWriteArrayList<Node>();
    }

    private Node getTaskExecuterNode() throws JobTrackerNotFoundException {
        try {
            if (taskExecuters.size() == 0) {
                throw new JobTrackerNotFoundException("no available jobTracker!");
            }
            // 连JobTracker的负载均衡算法
            LoadBalance loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig());
            return loadBalance.select(taskExecuters, appContext.getConfig().getIdentity());
        } catch (JobTrackerNotFoundException e) {
            this.serverEnable = false;
            // publish msg
            EventInfo eventInfo = new EventInfo(EcTopic.NO_TASK_EXECUTER_AVAILABLE);
            appContext.getEventCenter().publishAsync(eventInfo);
            throw e;
        }
    }

    public void start() {
        try {
            remotingClient.start();
        } catch (RemotingException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean contains(Node jobTracker) {
        return taskExecuters.contains(jobTracker);
    }

    public void addJobTracker(Node jobTracker) {
        if (!contains(jobTracker)) {
            taskExecuters.add(jobTracker);
        }
    }

    public boolean removeJobTracker(Node jobTracker) {
        return taskExecuters.remove(jobTracker);
    }

    /**
     * 同步调用
     */
    public RemotingCommand invokeSync(RemotingCommand request)
            throws JobTrackerNotFoundException {

        Node jobTracker = getTaskExecuterNode();

        try {
            RemotingCommand response = remotingClient.invokeSync(jobTracker.getAddress(),
                    request, appContext.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;
            return response;
        } catch (Exception e) {
            // 将这个JobTracker移除
            taskExecuters.remove(jobTracker);
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
            throws JobTrackerNotFoundException {

        Node taskExecuter = getTaskExecuterNode();

        try {
            remotingClient.invokeAsync(taskExecuter.getAddress(), request,
                    appContext.getConfig().getInvokeTimeoutMillis(), asyncCallback);
            this.serverEnable = true;
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
    public void invokeAsync(Channel channel,RemotingCommand request, AsyncCallback asyncCallback)
            throws JobTrackerNotFoundException {
        try {
            remotingClient.invokeAsync(channel, request,
                    appContext.getConfig().getInvokeTimeoutMillis(), asyncCallback);
            this.serverEnable = true;
        } catch (Throwable e) {
        }
    }
    /**
     * 单向调用
     */
    public void invokeOneway(RemotingCommand request)
            throws JobTrackerNotFoundException {

        Node jobTracker = getTaskExecuterNode();

        try {
            remotingClient.invokeOneway(jobTracker.getAddress(), request,
                    appContext.getConfig().getInvokeTimeoutMillis());
            this.serverEnable = true;
        } catch (Throwable e) {
            // 将这个JobTracker移除
            taskExecuters.remove(jobTracker);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e1) {
                LOGGER.error(e1.getMessage(), e1);
            }
            // 只要不是节点 不可用, 轮询所有节点请求
            invokeOneway(request);
        }
    }

    public void registerProcessor(int requestCode, RemotingProcessor processor,
                                  ExecutorService executor) {
        remotingClient.registerProcessor(requestCode, processor, executor);
    }

    public void registerDefaultProcessor(RemotingProcessor processor, ExecutorService executor) {
        remotingClient.registerDefaultProcessor(processor, executor);
    }

    public boolean isServerEnable() {
        return serverEnable;
    }

    public void setServerEnable(boolean serverEnable) {
        this.serverEnable = serverEnable;
    }

    public void shutdown() {
        remotingClient.shutdown();
    }

    public RemotingClient getRemotingClient() {
        return remotingClient;
    }
}
