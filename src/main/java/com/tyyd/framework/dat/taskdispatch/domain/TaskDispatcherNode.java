package com.tyyd.framework.dat.taskdispatch.domain;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 * TaskDispatcherNode
 */
public class TaskDispatcherNode extends Node {

    public TaskDispatcherNode() {
        this.setNodeType(NodeType.TASK_DISPATCH);
        this.addListenNodeType(NodeType.TASK_EXECUTER);
        this.addListenNodeType(NodeType.TASK_DISPATCH);
        this.addListenNodeType(NodeType.MONITOR);
    }
}
