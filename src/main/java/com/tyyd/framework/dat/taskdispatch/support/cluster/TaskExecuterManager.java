package com.tyyd.framework.dat.taskdispatch.support.cluster;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.factory.NodeFactory;
import com.tyyd.framework.dat.core.registry.RegistryFactory;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelWrapper;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.domain.TaskExecuterNode;

/**
 * TaskExecuter 管理器 (对 TaskExecuter 节点的记录 和 可用线程的记录)
 */
public class TaskExecuterManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecuterManager.class);
	private Set<TaskExecuterNode> taskExecuterNodes = new ConcurrentHashSet<TaskExecuterNode>();
	private TaskDispatcherAppContext appContext;

	public TaskExecuterManager(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * 添加节点
	 */
	public void addNode(Node node) {
		ChannelWrapper channel = appContext.getChannelManager().getChannel(node.getNodeType(), node.getIdentity());
		TaskExecuterNode taskTrackerNode = new TaskExecuterNode(node.getThreads(), node.getIdentity(), channel);
		taskTrackerNode.setIp(node.getIp());
		taskTrackerNode.setPort(node.getPort());
		LOGGER.info("Add TaskTracker node:{}", taskTrackerNode);
		taskExecuterNodes.add(taskTrackerNode);
	}

	/**
	 * 删除节点
	 *
	 * @param node
	 */
	public void removeNode(Node node) {
		if (taskExecuterNodes != null && taskExecuterNodes.size() != 0) {
			TaskExecuterNode taskTrackerNode = new TaskExecuterNode(node.getIdentity());
			LOGGER.info("Remove TaskTracker node:{}", taskTrackerNode);
			taskExecuterNodes.remove(taskTrackerNode);
		}
	}

	/**
	 * 删除节点
	 *
	 * @param node
	 */
	public void updateNode(Node node) {
		if (taskExecuterNodes != null && taskExecuterNodes.size() != 0) {
			for (TaskExecuterNode taskExecuterNode : taskExecuterNodes) {
				if (taskExecuterNode.getIdentity().equals(node.getIdentity())) {
					taskExecuterNode.setAvailableThread(node.getAvailableThreads());
				}
			}
		}
	}

	public TaskExecuterNode getTaskExecuterNode() {
		if (taskExecuterNodes == null || taskExecuterNodes.size() == 0) {
			return null;
		}
		TaskExecuterNode returnTaskExecuterNode = null;
		for (TaskExecuterNode taskExecuterNode : taskExecuterNodes) {
			if (null == returnTaskExecuterNode || returnTaskExecuterNode.getAvailableThreadInteger() < taskExecuterNode
					.getAvailableThreadInteger()) {
				returnTaskExecuterNode = taskExecuterNode;
			}
		}
		if (returnTaskExecuterNode == null) {
			return null;
		}
		if (returnTaskExecuterNode.getChannel() != null && returnTaskExecuterNode.getChannel().isClosed()) {
			// 只有当channel正常的时候才返回
			return returnTaskExecuterNode;
		}
		// 如果 channel 已经关闭, 更新channel, 如果没有channel, 略过
		ChannelWrapper channel = appContext.getChannelManager().getChannel(NodeType.TASK_EXECUTER,
				returnTaskExecuterNode.getIdentity());
		if (channel == null) {

		} else {
			try {
				channel = new ChannelWrapper(
						appContext.getRemotingServer().getRemotingClient().getAndCreateChannel(
								returnTaskExecuterNode.getIp() + ":" + returnTaskExecuterNode.getPort()),
						NodeType.TASK_EXECUTER, returnTaskExecuterNode.getIdentity());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			appContext.getChannelManager().offerChannel(channel);
		}
		// 更新channel

		returnTaskExecuterNode.setChannel(channel);
		taskExecuterNodes.add(returnTaskExecuterNode);
		LOGGER.info("update node channel , taskTackerNode={}", returnTaskExecuterNode);
		return returnTaskExecuterNode;
	}

	public TaskExecuterNode getTaskTrackerNode(String identity) {
		if (taskExecuterNodes == null || taskExecuterNodes.size() == 0) {
			return null;
		}

		for (TaskExecuterNode taskTrackerNode : taskExecuterNodes) {
			if (taskTrackerNode.getIdentity().equals(identity)) {
				if (taskTrackerNode.getChannel() == null || taskTrackerNode.getChannel().isClosed()) {
					// 如果 channel 已经关闭, 更新channel, 如果没有channel, 略过
					ChannelWrapper channel = appContext.getChannelManager().getChannel(NodeType.TASK_EXECUTER,
							taskTrackerNode.getIdentity());
					if (channel != null) {
						// 更新channel
						taskTrackerNode.setChannel(channel);
						LOGGER.info("update node channel , taskTackerNode={}", taskTrackerNode);
						return taskTrackerNode;
					}
				} else {
					// 只有当channel正常的时候才返回
					return taskTrackerNode;
				}
			}
		}
		return null;
	}

	/**
	 * 更新节点的 可用线程数
	 * 
	 * @param timestamp
	 *            时间戳, 只有当 时间戳大于上次更新的时间 才更新可用线程数
	 */
	public void updateTaskTrackerAvailableThreads(String nodeGroup, String identity, Integer availableThreads,
			Long timestamp) {

		if (taskExecuterNodes != null && taskExecuterNodes.size() != 0) {
			for (TaskExecuterNode trackerNode : taskExecuterNodes) {
				if (trackerNode.getIdentity().equals(identity)
						&& (trackerNode.getTimestamp() == null || trackerNode.getTimestamp() <= timestamp)) {
					trackerNode.setAvailableThread(availableThreads);
					trackerNode.setTimestamp(timestamp);
					Node newNode = NodeFactory.deepCopy(appContext.getNode());
					newNode.setAvailableThreads(availableThreads);
					RegistryFactory.getRegistry(appContext).updateRegister(appContext.getNode().toFullString(),
							newNode);
					;
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("更新节点线程数: {}", trackerNode);
					}
				}
			}
		}
	}
}
