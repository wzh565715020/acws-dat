package com.tyyd.framework.dat.core.factory;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.utils.NetUtils;
import com.tyyd.framework.dat.core.exception.DatRuntimeException;
import com.tyyd.framework.dat.core.support.IpPortUtil;
import com.tyyd.framework.dat.core.support.SystemClock;

/**
 * 节点工厂类
 */
public class NodeFactory {

	public static <T extends Node> T create(Class<T> clazz, Config config) {
		try {
			T node = clazz.newInstance();
			node.setCreateTime(SystemClock.now());
			node.setIp(IpPortUtil.getLocalIP());
			node.setHostName(NetUtils.getLocalHostName());
			node.setThreads(config.getWorkThreads());
			node.setAvailableThreads(config.getWorkThreads());
			int listenPort = config.getListenPort();
			node.setPort(listenPort);
			node.setIdentity(config.getIdentity());
			node.setClusterName(config.getClusterName());
			return node;
		} catch (Exception e) {
			throw new DatRuntimeException("Create Node error: clazz=" + clazz, e);
		}
	}

	public static Node deepCopy(Node node) {
		Node newNode = new Node();
		newNode.setAvailableThreads(node.getAvailableThreads());
		newNode.setClusterName(node.getClusterName());
		newNode.setCreateTime(node.getCreateTime());
		newNode.setGroup(node.getGroup());
		newNode.setHostName(node.getHostName());
		newNode.setHttpCmdPort(node.getHttpCmdPort());
		newNode.setIdentity(node.getIdentity());
		newNode.setIp(node.getIp());
		newNode.setListenNodeTypes(node.getListenNodeTypes());
		newNode.setNodeType(node.getNodeType());
		newNode.setPort(node.getPort());
		newNode.setThreads(node.getThreads());
		return newNode;
	}
}
