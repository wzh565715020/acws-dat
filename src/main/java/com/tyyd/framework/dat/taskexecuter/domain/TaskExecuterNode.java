package com.tyyd.framework.dat.taskexecuter.domain;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 *         TaskTracker 节点
 */
public class TaskExecuterNode extends Node {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3937969847800101276L;

	public TaskExecuterNode() {
        this.setNodeType(NodeType.TASK_EXECUTER);
    }

}
