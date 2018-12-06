package com.tyyd.framework.dat.taskclient.domain;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 *         任务客户端节点
 */
public class TaskClientNode extends Node {

    public TaskClientNode() {
        this.setNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.MONITOR);
    }

}
