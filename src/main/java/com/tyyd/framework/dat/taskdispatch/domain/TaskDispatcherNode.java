package com.tyyd.framework.dat.taskdispatch.domain;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 * Job Tracker 节点
 */
public class TaskDispatcherNode extends Node {

    public TaskDispatcherNode() {
        this.setNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.JOB_CLIENT);
        this.addListenNodeType(NodeType.TASK_TRACKER);
        this.addListenNodeType(NodeType.JOB_TRACKER);
        this.addListenNodeType(NodeType.MONITOR);
    }
}
