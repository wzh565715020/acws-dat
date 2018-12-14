package com.tyyd.framework.dat.core;

import com.tyyd.framework.dat.cmd.HttpCmdServer;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.SubscribedNodeManager;
import com.tyyd.framework.dat.core.monitor.MStatReporter;
import com.tyyd.framework.dat.core.protocol.command.CommandBodyWrapper;
import com.tyyd.framework.dat.core.registry.RegistryStatMonitor;
import com.tyyd.framework.dat.ec.EventCenter;

/**
 *         用来存储 程序的数据
 */
public abstract class AppContext {

    // 节点配置信息
    private Config config;
    // 节点管理
    private SubscribedNodeManager subscribedNodeManager;
    // 节点通信CommandBody包装器
    private CommandBodyWrapper commandBodyWrapper;
    // 事件中心
    private EventCenter eventCenter;
    // 监控中心
    private MStatReporter mStatReporter;
    // 注册中心状态监控
    private RegistryStatMonitor registryStatMonitor;
    // 命令中心
    private HttpCmdServer httpCmdServer;
    
    private Node node;
    private Node masterNode;
    
    public MStatReporter getMStatReporter() {
        return mStatReporter;
    }

    public void setMStatReporter(MStatReporter mStatReporter) {
        this.mStatReporter = mStatReporter;
    }

    public EventCenter getEventCenter() {
        return eventCenter;
    }

    public void setEventCenter(EventCenter eventCenter) {
        this.eventCenter = eventCenter;
    }

    public CommandBodyWrapper getCommandBodyWrapper() {
        return commandBodyWrapper;
    }

    public void setCommandBodyWrapper(CommandBodyWrapper commandBodyWrapper) {
        this.commandBodyWrapper = commandBodyWrapper;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public SubscribedNodeManager getSubscribedNodeManager() {
        return subscribedNodeManager;
    }

    public void setSubscribedNodeManager(SubscribedNodeManager subscribedNodeManager) {
        this.subscribedNodeManager = subscribedNodeManager;
    }


    public RegistryStatMonitor getRegistryStatMonitor() {
        return registryStatMonitor;
    }

    public void setRegistryStatMonitor(RegistryStatMonitor registryStatMonitor) {
        this.registryStatMonitor = registryStatMonitor;
    }

    public HttpCmdServer getHttpCmdServer() {
        return httpCmdServer;
    }

    public void setHttpCmdServer(HttpCmdServer httpCmdServer) {
        this.httpCmdServer = httpCmdServer;
    }
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Node getMasterNode() {
		return masterNode;
	}

	public void setMasterNode(Node masterNode) {
		this.masterNode = masterNode;
	}
	
}
