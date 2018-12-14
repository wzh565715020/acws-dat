package com.tyyd.framework.dat.core.protocol.command;

/**
 * 取消(删除)任务
 */
public class TaskCancelRequest extends AbstractRemotingCommandBody{

	private static final long serialVersionUID = 2945964772160028674L;

	private String taskId;

    private String taskTrackerNodeGroup;

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
}
