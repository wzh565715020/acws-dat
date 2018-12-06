package com.tyyd.framework.dat.taskexecuter.processor;

import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

public abstract class AbstractProcessor implements RemotingProcessor {

    protected TaskExecuterAppContext appContext;

    protected AbstractProcessor(TaskExecuterAppContext appContext) {
        this.appContext = appContext;
    }
}
