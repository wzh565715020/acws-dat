package com.tyyd.framework.dat.biz.logger.domain;

import com.tyyd.framework.dat.admin.request.PaginationReq;

import java.util.Date;

public class TaskLoggerRequest extends PaginationReq {

    private String id;

    private Date startLogTime;

    private Date endLogTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
