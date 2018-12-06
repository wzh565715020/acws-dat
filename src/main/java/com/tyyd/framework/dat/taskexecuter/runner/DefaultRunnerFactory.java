package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

public class DefaultRunnerFactory implements RunnerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunnerFactory.class);
    private TaskExecuterAppContext appContext;

    public DefaultRunnerFactory(TaskExecuterAppContext appContext) {
        this.appContext = appContext;
    }

    public TaskRunner newRunner() {
        try {
            return (TaskRunner) appContext.getJobRunnerClass().newInstance();
        } catch (InstantiationException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return null;
    }
}
