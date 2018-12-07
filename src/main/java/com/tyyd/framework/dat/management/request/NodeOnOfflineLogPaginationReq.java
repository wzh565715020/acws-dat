package com.tyyd.framework.dat.management.request;

import java.util.Date;

import com.tyyd.framework.dat.admin.request.PaginationReq;

public class NodeOnOfflineLogPaginationReq extends PaginationReq {

    private Date startLogTime;

    private Date endLogTime;

    private String group;

    private String identity;

    private String event;

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Date getStartLogTime() {
        return startLogTime;
    }

    public void setStartLogTime(Date startLogTime) {
        this.startLogTime = startLogTime;
    }

    public Date getEndLogTime() {
        return endLogTime;
    }

    public void setEndLogTime(Date endLogTime) {
        this.endLogTime = endLogTime;
    }
}
