package com.tyyd.framework.dat.management.request;

import com.tyyd.framework.dat.core.cluster.NodeType;

public class NodeGroupRequest {

    private NodeType nodeType;

    private String nodeGroup;

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }
}
