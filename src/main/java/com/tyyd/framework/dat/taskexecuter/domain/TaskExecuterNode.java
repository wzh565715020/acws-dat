package com.tyyd.framework.dat.taskexecuter.domain;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 *         TaskTracker 节点
 */
public class TaskExecuterNode extends Node {

    public TaskExecuterNode() {
        this.setNodeType(NodeType.TASK_EXECUTER);
    }

}
