package com.tyyd.framework.dat.biz.logger.domain;

import com.tyyd.framework.dat.admin.request.PaginationReq;

import java.util.Date;

/**
 * @author   on 6/11/15.
 */
public class JobLoggerRequest extends PaginationReq {

    private String taskId;

    private String taskTrackerNodeGroup;

    private Date startLogTime;

    private Date endLogTime;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
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
