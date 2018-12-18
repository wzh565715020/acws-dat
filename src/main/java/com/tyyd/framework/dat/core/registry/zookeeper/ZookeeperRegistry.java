package com.tyyd.framework.dat.core.registry.zookeeper;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.registry.FailbackRegistry;
import com.tyyd.framework.dat.core.registry.NodeRegistryUtils;
import com.tyyd.framework.dat.core.registry.NotifyEvent;
import com.tyyd.framework.dat.core.registry.NotifyListener;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.zookeeper.ChildListener;
import com.tyyd.framework.dat.zookeeper.DataListener;
import com.tyyd.framework.dat.zookeeper.StateListener;
import com.tyyd.framework.dat.zookeeper.ZkClient;
import com.tyyd.framework.dat.zookeeper.ZookeeperTransporter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 节点注册器，并监听自己关注的节点
 */
public class ZookeeperRegistry extends FailbackRegistry {

	private ZkClient zkClient;
	// 用来记录父节点下的子节点的变化
	private final ConcurrentHashMap<String/* parentPath */, List<String/* children */>> cachedChildrenNodeMap;

	private final ConcurrentMap<Node, ConcurrentMap<NotifyListener, ChildListener>> zkListeners;

	private String clusterName;

	public ZookeeperRegistry(final AppContext appContext) {
		super(appContext);
		this.clusterName = appContext.getConfig().getClusterName();
		this.cachedChildrenNodeMap = new ConcurrentHashMap<String, List<String>>();
		ZookeeperTransporter zookeeperTransporter = ServiceLoader.load(ZookeeperTransporter.class,
				appContext.getConfig());
		this.zkClient = zookeeperTransporter.connect(appContext.getConfig());
		this.zkListeners = new ConcurrentHashMap<Node, ConcurrentMap<NotifyListener, ChildListener>>();
		// 默认是连成功的(用zkclient时候，第一次不会有state changed事件暴露给用户，
		// 他居然在new ZkClient的时候就直接连接了，给个提供listener的构造函数或者把启动改为start方法都ok呀，蛋疼)
		appContext.getRegistryStatMonitor().setAvailable(true);

		zkClient.addStateListener(new StateListener() {
			@Override
			public void stateChanged(int state) {
				if (state == DISCONNECTED) {
					appContext.getRegistryStatMonitor().setAvailable(false);
				} else if (state == CONNECTED) {
					appContext.getRegistryStatMonitor().setAvailable(true);
				} else if (state == RECONNECTED) {
					try {
						appContext.getRegistryStatMonitor().setAvailable(true);
						recover();
					} catch (Exception e) {
						LOGGER.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	@Override
	protected void doRegister(Node node) {
		if (zkClient.exists(node.toFullString())) {
			return;
		}
		zkClient.create(node.toFullString(), true, false);
	}

	@Override
	protected void doUnRegister(Node node) {
		zkClient.delete(node.toFullString());
	}

	@Override
	protected void doSubscribe(Node node, NotifyListener listener) {
		List<NodeType> listenNodeTypes = node.getListenNodeTypes();
		if (CollectionUtils.isEmpty(listenNodeTypes)) {
			return;
		}
		for (NodeType listenNodeType : listenNodeTypes) {
			String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);

			ChildListener zkListener = addZkListener(node, listener);

			// 为自己关注的 节点 添加监听
			List<String> children = zkClient.addChildListener(listenNodePath, zkListener);

			if (CollectionUtils.isNotEmpty(children)) {
				List<Node> listenedNodes = new ArrayList<Node>();
				for (String child : children) {
					Node listenedNode = NodeRegistryUtils.parse(listenNodePath + "/" + child);
					listenedNodes.add(listenedNode);
				}
				notify(NotifyEvent.ADD, listenedNodes, listener);
				cachedChildrenNodeMap.put(listenNodePath, children);
			}
		}
	}

	@Override
	protected void doUnsubscribe(Node node, NotifyListener listener) {
		ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(node);
		if (listeners != null) {
			ChildListener zkListener = listeners.get(listener);
			if (zkListener != null) {
				List<NodeType> listenNodeTypes = node.getListenNodeTypes();
				if (CollectionUtils.isEmpty(listenNodeTypes)) {
					return;
				}
				for (NodeType listenNodeType : listenNodeTypes) {
					String listenNodePath = NodeRegistryUtils.getNodeTypePath(clusterName, listenNodeType);
					zkClient.removeChildListener(listenNodePath, zkListener);
				}
			}
		}
	}

	private ChildListener addZkListener(Node node, final NotifyListener listener) {

		ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(node);
		if (listeners == null) {
			zkListeners.putIfAbsent(node, new ConcurrentHashMap<NotifyListener, ChildListener>());
			listeners = zkListeners.get(node);
		}
		ChildListener zkListener = listeners.get(listener);
		if (zkListener == null) {

			listeners.putIfAbsent(listener, new ChildListener() {

				public void childChanged(String parentPath, List<String> currentChildren) {

					if (CollectionUtils.isEmpty(currentChildren)) {
						currentChildren = new ArrayList<String>(0);
					}

					List<String> oldChildren = cachedChildrenNodeMap.get(parentPath);
					List<Node> currentNodeChildren = new ArrayList<Node>(currentChildren.size());
					for (String child : currentChildren) {
						Node node = NodeRegistryUtils.parse(parentPath + "/" + child);
						currentNodeChildren.add(node);
					}
					
					List<Node> oldNodeChildren = null;
					if (oldChildren != null && !oldChildren.isEmpty()) {
						oldNodeChildren = new ArrayList<Node>(oldChildren.size());
						for (String child : oldChildren) {
							Node node = NodeRegistryUtils.parse(parentPath + "/" + child);
							oldNodeChildren.add(node);
						}
					}
					// 1. 找出增加的 节点
					List<Node> addChildren = CollectionUtils.getLeftDiff(currentNodeChildren, oldNodeChildren);
					// 2. 找出减少的 节点
					List<Node> decChildren = CollectionUtils.getLeftDiff(oldNodeChildren, currentNodeChildren);
					// 3. 找出相同的 节点
					List<Node> sameChildren = CollectionUtils.getNotDiff(currentNodeChildren, oldNodeChildren);

					if (CollectionUtils.isNotEmpty(addChildren)) {
						ZookeeperRegistry.this.notify(NotifyEvent.ADD, addChildren, listener);
					}

					if (CollectionUtils.isNotEmpty(decChildren)) {
						ZookeeperRegistry.this.notify(NotifyEvent.REMOVE, decChildren, listener);
					}
					if (CollectionUtils.isNotEmpty(sameChildren)) {
						ZookeeperRegistry.this.notify(NotifyEvent.UPDATE, sameChildren, listener);
					}
					cachedChildrenNodeMap.put(parentPath, currentChildren);
				}
			});
			zkListener = listeners.get(listener);
		}
		return zkListener;
	}

	@Override
	public void destroy() {
		super.destroy();
		try {
			zkClient.close();
		} catch (Exception e) {
			LOGGER.warn("Failed to close zookeeper client " + getNode() + ", cause: " + e.getMessage(), e);
		}
	}

	@Override
	public void updateRegister(String path, Node data) {
		zkClient.setData(path, data);
		
	}
	@Override
	public void addDataListener(String path, DataListener listener) {
		if (!zkClient.exists(path)) {
			zkClient.create(path, true, false);
		}
		zkClient.addDataListener(path, listener);
	}
}
