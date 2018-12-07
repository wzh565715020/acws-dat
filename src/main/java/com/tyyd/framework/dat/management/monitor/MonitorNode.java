package com.tyyd.framework.dat.management.monitor;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;

public class MonitorNode extends Node {

    public MonitorNode() {
        this.setNodeType(NodeType.MONITOR);
    }
}
