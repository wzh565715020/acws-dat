package com.tyyd.framework.dat.taskexecuter.domain;

import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.remoting.Channel;

public class Response {

    private Action action;

    private String msg;

    private TaskMeta taskMeta;
    
    private Channel channel;
    /**
     * 是否接收新任务
     */
    private boolean receiveNewJob = true;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public TaskMeta getTaskMeta() {
        return taskMeta;
    }

    public void setJobMeta(TaskMeta jobMeta) {
        this.taskMeta = jobMeta;
    }

    public boolean isReceiveNewJob() {
        return receiveNewJob;
    }

    public void setReceiveNewJob(boolean receiveNewJob) {
        this.receiveNewJob = receiveNewJob;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}
    
}
