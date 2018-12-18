package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.taskexecuter.Result;

/**
 *         任务执行者要实现的接口
 */
public interface TaskRunner {

    /**
     * 执行任务
     * 抛出异常则消费失败, 返回null则认为是消费成功
     */
    public Result run(Task task) throws Throwable;

}
