package com.tyyd.framework.dat.management.request;

import com.tyyd.framework.dat.admin.request.PaginationReq;
import com.tyyd.framework.dat.core.cluster.NodeType;

/**
 * @author   on 8/22/15.
 */
public class MDataPaginationReq extends PaginationReq {

    private NodeType nodeType;

    private String id;

    private String nodeGroup;

    private String identity;

    private Long startTime;

    private Long endTime;

    private JVMType jvmType;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public JVMType getJvmType() {
        return jvmType;
    }

    public void setJvmType(JVMType jvmType) {
        this.jvmType = jvmType;
    }
}
