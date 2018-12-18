package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.core.cluster.DATConfig;
import com.tyyd.framework.dat.core.constant.Environment;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.logger.BizLoggerFactory;

/**
 * 为了方便JobRunner测试设计的
 *
 */
public abstract class TaskRunnerTester {

    public Result run(Task task) throws Throwable {
        // 1. 设置LTS环境为 UNIT_TEST
        DATConfig.setEnvironment(Environment.UNIT_TEST);
        // 设置 BizLogger
        DatLoggerFactory.setLogger(BizLoggerFactory.getLogger(Level.INFO, null, null));
        // 2. load context (Spring Context 或者其他的)
        initContext();
        // 3. new jobRunner
        TaskRunner jobRunner = newJobRunner();
        // 4. run task
        return jobRunner.run(task);
    }

    /**
     * 初始化上下文 (Spring Context等),准备运行环境
     */
    protected abstract void initContext();

    /**
     * 创建JobRunner
     */
    protected abstract TaskRunner newJobRunner();

}
