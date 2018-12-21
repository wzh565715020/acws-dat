package com.tyyd.framework.dat.taskexecuter.runner;

/**
 *         task Runner 的工厂类
 */
public interface RunnerFactory {

    public TaskRunner newRunner(String taskRunnerBeanName);

}
