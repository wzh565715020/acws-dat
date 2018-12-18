package com.tyyd.framework.dat.taskdispatch.domain;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 * TaskDispatcherNode
 */
public class TaskDispatcherNode extends Node {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2301281177872533692L;

	public TaskDispatcherNode() {
        this.setNodeType(NodeType.TASK_DISPATCH);
        this.addListenNodeType(NodeType.TASK_EXECUTER);
        this.addListenNodeType(NodeType.TASK_DISPATCH);
    }
}
