package com.tyyd.framework.dat.core.domain.monitor;

import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 * @author Robert HG (254963746@qq.com) on 3/11/16.
 */
public class MNode {

    private NodeType nodeType;
    /**
     * TaskTracker 节点标识
     */
    private String identity;

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
