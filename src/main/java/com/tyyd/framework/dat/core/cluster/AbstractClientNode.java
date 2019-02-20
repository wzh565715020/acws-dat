package com.tyyd.framework.dat.core.cluster;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.remoting.RemotingClient;
import com.tyyd.framework.dat.remoting.RemotingClientConfig;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.RemotingServer;
import com.tyyd.framework.dat.remoting.RemotingServerConfig;
import com.tyyd.framework.dat.remoting.RemotingTransporter;

import java.util.concurrent.Executors;

public abstract class AbstractClientNode<T extends Node, Context extends AppContext> extends AbstractTaskClientNode<T, Context> {

    protected RemotingClientDelegate remotingClient;
    protected RemotingServerDelegate remotingServer;
    

    protected void remotingStart() {
        remotingClient.start();
        remotingServer.start();
        RemotingProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = config.getParameter(Constants.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
            remotingClient.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize,
                            new NamedThreadFactory(AbstractClientNode.class.getSimpleName(), true)));
            remotingServer.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize,
                            new NamedThreadFactory(AbstractClientNode.class.getSimpleName(), true)));
        }
    }

    /**
     * 得到默认的处理器
     */
    protected abstract RemotingProcessor getDefaultProcessor();

    protected void remotingStop() {
        remotingClient.shutdown();
        remotingServer.shutdown();
    }
    
    public void setListenPort(int listenPort) {
        config.setListenPort(listenPort);
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
    	RemotingServerConfig remotingServerConfig = new RemotingServerConfig();
        // config 配置
        if (config.getListenPort() == 0) {
            config.setListenPort(Constants.TASK_EXECUTER_DEFAULT_LISTEN_PORT);
            node.setPort(config.getListenPort());
        }
        remotingServerConfig.setListenPort(config.getListenPort());
        node.setIdentity(node.getClusterName() + "_" + node.getIp() + "_" + node.getPort());
        appContext.getConfig().setIdentity(node.getIdentity());
        this.remotingServer = new RemotingServerDelegate(getRemotingServer(remotingServerConfig), appContext);
        this.remotingClient = new RemotingClientDelegate(getRemotingClient(new RemotingClientConfig()), appContext);

        beforeStart();
    }
    private RemotingServer getRemotingServer(RemotingServerConfig remotingServerConfig) {
        return ServiceLoader.load(RemotingTransporter.class, config).getRemotingServer(appContext, remotingServerConfig);
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
