package com.tyyd.framework.dat.core.registry;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.zookeeper.DataListener;

/**
 *         节点注册接口
 */
public interface Registry {

    /**
     * 节点注册
     */
    void register(Node node);

    /**
     * 节点 取消注册
     */
    void unregister(Node node);

    /**
     * 监听节点
     */
    void subscribe(Node node, NotifyListener listener);

    /**
     * 取消监听节点
     */
    void unsubscribe(Node node, NotifyListener listener);

    /**
     * 销毁
     */
    void destroy();
    /**
     * 节点更新
     */
    void updateRegister(String path,Node node);

	void addDataListener(String path, DataListener listener);
}
