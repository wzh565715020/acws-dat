package com.tyyd.framework.dat.taskdispatch.processor;

import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public abstract class AbstractRemotingProcessor implements RemotingProcessor {

    protected TaskDispatcherAppContext appContext;

    public AbstractRemotingProcessor(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

}
