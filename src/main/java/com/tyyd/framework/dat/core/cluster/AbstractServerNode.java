package com.tyyd.framework.dat.core.cluster;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.RemotingServer;
import com.tyyd.framework.dat.remoting.RemotingServerConfig;
import com.tyyd.framework.dat.remoting.RemotingTransporter;

import java.util.concurrent.Executors;

/**
 *         抽象服务端
 */
public abstract class AbstractServerNode<T extends Node, App extends AppContext> extends AbstractTaskNode<T, App> {

    protected RemotingServerDelegate remotingServer;

    protected void remotingStart() {

        remotingServer.start();

        RemotingProcessor defaultProcessor = getDefaultProcessor();
        if (defaultProcessor != null) {
            int processorSize = config.getParameter(Constants.PROCESSOR_THREAD, Constants.DEFAULT_PROCESSOR_THREAD);
            remotingServer.registerDefaultProcessor(defaultProcessor,
                    Executors.newFixedThreadPool(processorSize, new NamedThreadFactory(AbstractServerNode.class.getSimpleName(), true)));
        }
    }

    public void setListenPort(int listenPort) {
        config.setListenPort(listenPort);
    }

    protected void remotingStop() {
        remotingServer.shutdown();
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

        remotingServer = new RemotingServerDelegate(getRemotingServer(remotingServerConfig), appContext);

        beforeStart();
    }

    private RemotingServer getRemotingServer(RemotingServerConfig remotingServerConfig) {
        return ServiceLoader.load(RemotingTransporter.class, config).getRemotingServer(appContext, remotingServerConfig);
    }

    @Override
    protected void afterRemotingStart() {
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

    /**
     * 得到默认的处理器
     */
    protected abstract RemotingProcessor getDefaultProcessor();

    protected abstract void beforeStart();

    protected abstract void afterStart();

    protected abstract void afterStop();

    protected abstract void beforeStop();


}
