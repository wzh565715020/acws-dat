package com.tyyd.framework.dat.core.listener;

import com.tyyd.framework.dat.core.cluster.Node;

import java.util.List;

public interface NodeChangeListener {

    /**
     * 添加节点
     *
     * @param nodes 节点列表
     */
    public void addNodes(List<Node> nodes);

    /**
     * 移除节点
     * @param nodes 节点列表
     */
    public void removeNodes(List<Node> nodes);
    /**
     * 更新节点
     * @param nodes 节点列表
     */
    public void updateNodes(List<Node> nodes);

}
