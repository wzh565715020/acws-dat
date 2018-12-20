package com.tyyd.framework.dat.taskdispatch.support.cluster;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.factory.NodeFactory;
import com.tyyd.framework.dat.core.registry.RegistryFactory;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 * TaskExecuter 管理器 (对 TaskExecuter 节点的记录 和 可用线程的记录)
 */
public class TaskExecuterManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecuterManager.class);
	private TaskDispatcherAppContext appContext;

	public TaskExecuterManager(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * 更新节点的 可用线程数
	 * 
	 */
	public void updateTaskTrackerAvailableThreads(Node node) {
		Node newNode = NodeFactory.deepCopy(node);
		newNode.setAvailableThreads(node.getAvailableThreads() - 1);
		RegistryFactory.getRegistry(appContext).updateRegister(node.toFullString(), newNode);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("更新节点线程数: {}", newNode);
		}
	}
}
