package com.tyyd.framework.dat.taskclient.domain;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.taskclient.support.TaskCompletedHandler;

public class TaskClientAppContext extends AppContext {

    private RemotingClientDelegate remotingClient;

    private TaskCompletedHandler jobCompletedHandler;

    public TaskCompletedHandler getJobCompletedHandler() {
        return jobCompletedHandler;
    }

    public void setJobCompletedHandler(TaskCompletedHandler jobCompletedHandler) {
        this.jobCompletedHandler = jobCompletedHandler;
    }

    public RemotingClientDelegate getRemotingClient() {
        return remotingClient;
    }

    public void setRemotingClient(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }
}

