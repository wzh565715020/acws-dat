package com.tyyd.framework.dat.core.cluster;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.ListUtils;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.listener.NodeChangeListener;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.ec.EventInfo;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点管理 (主要用于管理自己关注的节点)
 */
public class SubscribedNodeManager implements NodeChangeListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubscribedNodeManager.class);
	private final ConcurrentHashMap<NodeType, Set<Node>> NODES = new ConcurrentHashMap<NodeType, Set<Node>>();

	private AppContext appContext;

	public SubscribedNodeManager(AppContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * 添加监听的节点
	 */
	private void addNode(Node node) {
		_addNode(node);
	}

	private void _addNode(Node node) {
		Set<Node> nodeSet = NODES.get(node.getNodeType());
		if (CollectionUtils.isEmpty(nodeSet)) {
			nodeSet = new ConcurrentHashSet<Node>();
			Set<Node> oldNodeList = NODES.putIfAbsent(node.getNodeType(), nodeSet);
			if (oldNodeList != null) {
				nodeSet = oldNodeList;
			}
		}
		nodeSet.add(node);
		EventInfo eventInfo = new EventInfo(EcTopic.NODE_ADD);
		eventInfo.setParam("node", node);
		appContext.getEventCenter().publishSync(eventInfo);
		LOGGER.info("Add {}", node);
	}

	public List<Node> getNodeList(final NodeType nodeType, final String nodeGroup) {

		Set<Node> nodes = NODES.get(nodeType);

		return ListUtils.filter(CollectionUtils.setToList(nodes), new ListUtils.Filter<Node>() {
			@Override
			public boolean filter(Node node) {
				return node.getGroup().equals(nodeGroup);
			}
		});
	}
	public Node getNode(NodeType nodeType,String nodeId) {
		Set<Node> nodeSet = NODES.get(nodeType);
		if (CollectionUtils.isEmpty(nodeSet)) {
			return null;
		}
		for (Node node : nodeSet) {
			if (node.getIdentity().equals(nodeId)) {
				return node;
			}
		}
		return null;
	}
	public List<Node> getNodeList(NodeType nodeType) {
		return CollectionUtils.setToList(NODES.get(nodeType));
	}

	private void removeNode(Node delNode) {
		Set<Node> nodeSet = NODES.get(delNode.getNodeType());

		if (CollectionUtils.isNotEmpty(nodeSet)) {
			for (Node node : nodeSet) {
				if (node.getIdentity().equals(delNode.getIdentity())) {
					nodeSet.remove(node);
					EventInfo eventInfo = new EventInfo(EcTopic.NODE_REMOVE);
					eventInfo.setParam("node", node);
					appContext.getEventCenter().publishSync(eventInfo);
					LOGGER.info("Remove {}", node);
				}
			}
		}
	}

	@Override
	public void addNodes(List<Node> nodes) {
		if (CollectionUtils.isEmpty(nodes)) {
			return;
		}
		for (Node node : nodes) {
			addNode(node);
		}
	}

	@Override
	public void removeNodes(List<Node> nodes) {
		if (CollectionUtils.isEmpty(nodes)) {
			return;
		}
		for (Node node : nodes) {
			removeNode(node);
		}
	}

	@Override
	public void updateNodes(List<Node> nodes) {
		if (CollectionUtils.isEmpty(nodes)) {
			return;
		}
		for (Node node : nodes) {
			updateNode(node);
		}
	}

	private void updateNode(Node node) {
		Set<Node> nodeSet = NODES.get(node.getNodeType());
		if (CollectionUtils.isNotEmpty(nodeSet)) {
			for (Node temp : nodeSet) {
				if (temp.getIdentity().equals(node.getIdentity())) {
					temp.setAvailableThreads(node.getAvailableThreads());
				}
			}
		}

	}
}
