package com.tyyd.framework.dat.core.factory;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.utils.NetUtils;
import com.tyyd.framework.dat.core.exception.LtsRuntimeException;
import com.tyyd.framework.dat.core.support.SystemClock;

/**
 * @author Robert HG (254963746@qq.com) on 7/25/14.
 *         节点工厂类
 */
public class NodeFactory {

    public static <T extends Node> T create(Class<T> clazz, Config config) {
        try {
            T node = clazz.newInstance();
            node.setCreateTime(SystemClock.now());
            node.setIp(config.getIp());
            node.setHostName(NetUtils.getLocalHostName());
            node.setGroup(config.getNodeGroup());
            node.setThreads(config.getWorkThreads());
            node.setPort(config.getListenPort());
            node.setIdentity(config.getIdentity());
            node.setClusterName(config.getClusterName());
            return node;
        } catch (Exception e) {
            throw new LtsRuntimeException("Create Node error: clazz=" + clazz, e);
        }
    }
}
