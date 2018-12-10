package com.tyyd.framework.dat.core.protocol.command;

public class TaskPullRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 9222159289387747395L;
	
	private Integer availableThreads;

    public Integer getAvailableThreads() {
        return availableThreads;
    }

    public void setAvailableThreads(Integer availableThreads) {
        this.availableThreads = availableThreads;
    }
}
