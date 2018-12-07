package com.tyyd.framework.dat.management.request;


import java.util.Date;

import com.tyyd.framework.dat.admin.request.PaginationReq;
import com.tyyd.framework.dat.core.cluster.NodeType;

public class NodePaginationReq extends PaginationReq {

    private String identity;
    private String ip;
    private String nodeGroup;
    private NodeType nodeType;
    private Boolean available;
    private Date startDate;
    private Date endDate;

    public NodePaginationReq() {
        // 默认不分页
        setLimit(Integer.MAX_VALUE);
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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
