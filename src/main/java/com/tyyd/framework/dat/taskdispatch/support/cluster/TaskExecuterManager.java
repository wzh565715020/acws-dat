package com.tyyd.framework.dat.taskdispatch.support.cluster;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelWrapper;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.domain.TaskExecuterNode;

/**
 * TaskExecuter 管理器 (对 TaskExecuter 节点的记录 和 可用线程的记录)
 */
public class TaskExecuterManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecuterManager.class);
	// 单例
	private final ConcurrentHashMap<String, Set<TaskExecuterNode>> NODE_MAP = new ConcurrentHashMap<String, Set<TaskExecuterNode>>();
	
	private TaskDispatcherAppContext appContext;

	public TaskExecuterManager(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * get all connected node group
	 */
	public Set<String> getNodeGroups() {
		return NODE_MAP.keySet();
	}

	/**
	 * 添加节点
	 */
	public void addNode(Node node) {
		// channel 可能为 null
		ChannelWrapper channel = appContext.getChannelManager().getChannel(node.getGroup(), node.getNodeType(),
				node.getIdentity());
		Set<TaskExecuterNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());

		if (taskTrackerNodes == null) {
			taskTrackerNodes = new ConcurrentHashSet<TaskExecuterNode>();
			Set<TaskExecuterNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), taskTrackerNodes);
			if (oldSet != null) {
				taskTrackerNodes = oldSet;
			}
		}

		TaskExecuterNode taskTrackerNode = new TaskExecuterNode(node.getGroup(), node.getThreads(), node.getIdentity(),
				channel);
		taskTrackerNode.setIp(node.getIp());
		taskTrackerNode.setPort(node.getPort());
		LOGGER.info("Add TaskTracker node:{}", taskTrackerNode);
		taskTrackerNodes.add(taskTrackerNode);

		// create executable queue
		// appContext.getExecutableJobQueue().createQueue(node.getGroup());
		// appContext.getNodeGroupStore().addNodeGroup(NodeType.TASK_TRACKER,
		// node.getGroup());
	}

	/**
	 * 删除节点
	 *
	 * @param node
	 */
	public void removeNode(Node node) {
		Set<TaskExecuterNode> taskTrackerNodes = NODE_MAP.get(node.getGroup());
		if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
			TaskExecuterNode taskTrackerNode = new TaskExecuterNode(node.getIdentity());
			taskTrackerNode.setNodeGroup(node.getGroup());
			LOGGER.info("Remove TaskTracker node:{}", taskTrackerNode);
			taskTrackerNodes.remove(taskTrackerNode);
		}
	}

	public TaskExecuterNode getTaskTrackerNode(String nodeGroup) {
		Set<TaskExecuterNode> taskExecuterNodes = NODE_MAP.get(nodeGroup);
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
		ChannelWrapper channel = appContext.getChannelManager().getChannel(returnTaskExecuterNode.getNodeGroup(),
				NodeType.TASK_EXECUTER, returnTaskExecuterNode.getIdentity());
		if (channel == null) {

		} else {
			try {
				channel = new ChannelWrapper(appContext.getRemotingServer().getRemotingClient()
						.getAndCreateChannel(returnTaskExecuterNode.getIp() + ":" + returnTaskExecuterNode.getPort()), NodeType.TASK_EXECUTER, returnTaskExecuterNode.getNodeGroup(), returnTaskExecuterNode.getIdentity());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		// 更新channel
		returnTaskExecuterNode.setChannel(channel);
		taskExecuterNodes.add(returnTaskExecuterNode);
		LOGGER.info("update node channel , taskTackerNode={}", returnTaskExecuterNode);
		return returnTaskExecuterNode;
	}

	public TaskExecuterNode getTaskTrackerNode(String nodeGroup, String identity) {
		Set<TaskExecuterNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);
		if (taskTrackerNodes == null || taskTrackerNodes.size() == 0) {
			return null;
		}

		for (TaskExecuterNode taskTrackerNode : taskTrackerNodes) {
			if (taskTrackerNode.getIdentity().equals(identity)) {
				if (taskTrackerNode.getChannel() == null || taskTrackerNode.getChannel().isClosed()) {
					// 如果 channel 已经关闭, 更新channel, 如果没有channel, 略过
					ChannelWrapper channel = appContext.getChannelManager().getChannel(taskTrackerNode.getNodeGroup(),
							NodeType.TASK_EXECUTER, taskTrackerNode.getIdentity());
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

		Set<TaskExecuterNode> taskTrackerNodes = NODE_MAP.get(nodeGroup);

		if (taskTrackerNodes != null && taskTrackerNodes.size() != 0) {
			for (TaskExecuterNode trackerNode : taskTrackerNodes) {
				if (trackerNode.getIdentity().equals(identity)
						&& (trackerNode.getTimestamp() == null || trackerNode.getTimestamp() <= timestamp)) {
					trackerNode.setAvailableThread(availableThreads);
					trackerNode.setTimestamp(timestamp);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("更新节点线程数: {}", trackerNode);
					}
				}
			}
		}
	}
}
