package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.core.cluster.LTSConfig;
import com.tyyd.framework.dat.core.constant.Environment;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.logger.BizLoggerFactory;

/**
 * 为了方便JobRunner测试设计的
 *
 */
public abstract class TaskRunnerTester {

    public Result run(Job job) throws Throwable {
        // 1. 设置LTS环境为 UNIT_TEST
        LTSConfig.setEnvironment(Environment.UNIT_TEST);
        // 设置 BizLogger
        DtaLoggerFactory.setLogger(BizLoggerFactory.getLogger(Level.INFO, null, null));
        // 2. load context (Spring Context 或者其他的)
        initContext();
        // 3. new jobRunner
        TaskRunner jobRunner = newJobRunner();
        // 4. run job
        return jobRunner.run(job);
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
