package com.tyyd.framework.dat.taskexecuter.runner;

/**
 * 实现这个类可以自定义在中断时候的操作
 */
public interface InterruptibleTaskRunner extends TaskRunner {

    /**
     * 当任务被cancel(中断)的时候,调用这个
     */
    void interrupt();
}
