package com.tyyd.framework.dat.taskdispatch.support.cluster;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.factory.NodeFactory;
import com.tyyd.framework.dat.core.registry.RegistryFactory;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 * TaskExecuter 管理器 (对 TaskExecuter 节点的记录 和 可用线程的记录)
 */
public class TaskExecuterManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecuterManager.class);
	private Set<Node> taskExecuterNodes = new ConcurrentHashSet<Node>();
	private TaskDispatcherAppContext appContext;

	public TaskExecuterManager(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * 添加节点
	 */
	public void addNode(Node node) {
		LOGGER.info("Add TaskExecuter node:{}", node);
		taskExecuterNodes.add(node);
	}

	/**
	 * 删除节点
	 *
	 * @param node
	 */
	public void removeNode(Node node) {
		if (taskExecuterNodes != null && taskExecuterNodes.size() != 0) {
			LOGGER.info("Remove TaskExecuter node:{}", node);
			taskExecuterNodes.remove(node);
		}
	}

	/**
	 * 删除节点
	 *
	 * @param node
	 */
	public void updateNode(Node node) {
		if (taskExecuterNodes != null && taskExecuterNodes.size() != 0) {
			for (Node taskExecuterNode : taskExecuterNodes) {
				if (taskExecuterNode.getIdentity().equals(node.getIdentity())) {
					taskExecuterNode.setAvailableThreads(node.getAvailableThreads());
				}
			}
		}
	}


	/**
	 * 更新节点的 可用线程数
	 * 
	 */
	public void updateTaskTrackerAvailableThreads(String identity, Integer availableThreads) {

		if (taskExecuterNodes != null && taskExecuterNodes.size() != 0) {
			for (Node trackerNode : taskExecuterNodes) {
				if (trackerNode.getIdentity().equals(identity)) {
					Node newNode = NodeFactory.deepCopy(trackerNode);
					newNode.setAvailableThreads(availableThreads);
					RegistryFactory.getRegistry(appContext).updateRegister(trackerNode.toFullString(),
							newNode);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("更新节点线程数: {}", trackerNode);
					}
				}
			}
		}
	}
}
