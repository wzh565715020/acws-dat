package com.tyyd.framework.dat.management.cluster;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

public class BackendNode extends Node {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8269980926450746989L;

	public BackendNode() {
        this.setNodeType(NodeType.TASK_DISPATCH);
        this.addListenNodeType(NodeType.TASK_CLIENT);
        this.addListenNodeType(NodeType.TASK_EXECUTER);
        this.addListenNodeType(NodeType.TASK_DISPATCH);
        this.addListenNodeType(NodeType.MONITOR);
    }
}
