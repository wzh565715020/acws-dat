package com.tyyd.framework.dat.core.cluster;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.remoting.HeartBeatMonitor;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.remoting.RemotingClient;
import com.tyyd.framework.dat.remoting.RemotingClientConfig;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.RemotingTransporter;

import java.util.concurrent.Executors;

/**
 *         抽象客户端
 */
public abstract class AbstractClientNode<T extends Node, Context extends AppContext> extends AbstractJobClientNode<T, Context> {

    protected RemotingClientDelegate remotingClient;
    private HeartBeatMonitor heartBeatMonitor;

    protected void remotingStart() {
        remotingClient.start();
        heartBeatMonitor.start();

        RemotingProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = config.getParameter(Constants.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
            remotingClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize,
                            new NamedThreadFactory(AbstractClientNode.class.getSimpleName(), true)));
        }
    }

    /**
     * 得到默认的处理器
     */
    protected abstract RemotingProcessor getDefaultProcessor();

    protected void remotingStop() {
        heartBeatMonitor.stop();
        remotingClient.shutdown();
    }

    /**
     * 设置节点组名
     */
    public void setNodeGroup(String nodeGroup) {
        config.setNodeGroup(nodeGroup);
    }

    public boolean isServerEnable() {
        return remotingClient.isServerEnable();
    }

    /**
     * 设置连接JobTracker的负载均衡算法
     *
     * @param loadBalance 算法 random, consistenthash
     */
    public void setLoadBalance(String loadBalance) {
        config.setParameter("loadbalance", loadBalance);
    }


    @Override
    protected void beforeRemotingStart() {
        //
        this.remotingClient = new RemotingClientDelegate(getRemotingClient(new RemotingClientConfig()), appContext);
        this.heartBeatMonitor = new HeartBeatMonitor(remotingClient, appContext);

        beforeStart();
    }

    private RemotingClient getRemotingClient(RemotingClientConfig remotingClientConfig) {
        return ServiceLoader.load(RemotingTransporter.class, config).getRemotingClient(appContext, remotingClientConfig);
    }

    @Override
    protected void afterRemotingStart() {
        // 父类要做的
        afterStart();
    }

    @Override
    protected void beforeRemotingStop() {
        beforeStop();
    }

    @Override
    protected void afterRemotingStop() {
        afterStop();
    }

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();

}
