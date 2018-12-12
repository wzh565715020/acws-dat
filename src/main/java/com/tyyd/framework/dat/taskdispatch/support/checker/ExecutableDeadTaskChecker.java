package com.tyyd.framework.dat.taskdispatch.support.checker;


import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 * to fix the executable dead job
 *
 */
public class ExecutableDeadTaskChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableDeadTaskChecker.class);

    // 1 分钟还锁着的，说明是有问题的
    private static final long MAX_TIME_OUT = 60 * 1000;

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-ExecutableJobQueue-Fix-Executor", true));

    private TaskDispatcherAppContext appContext;

    public ExecutableDeadTaskChecker(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    private AtomicBoolean start = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
                            if (!appContext.getRegistryStatMonitor().isAvailable()) {
                                return;
                            }
                            fix();
                        } catch (Throwable t) {
                            LOGGER.error(t.getMessage(), t);
                        }
                    }
                }, 30, 60, TimeUnit.SECONDS);// 3分钟执行一次
            }
            LOGGER.info("Executable dead job checker started!");
        } catch (Throwable t) {
            LOGGER.info("Executable dead job checker start failed!");
        }
    }

    /**
     * fix the job that running is true and gmtModified too old
     */
    private void fix() {
            List<TaskPo> deadJobPo = appContext.getExecutableJobQueue().getDeadJob(SystemClock.now() - MAX_TIME_OUT);
            if (CollectionUtils.isNotEmpty(deadJobPo)) {
                for (TaskPo jobPo : deadJobPo) {
                    appContext.getExecutableJobQueue().resume(jobPo);
                    LOGGER.info("Fix executable job : {} ", JSON.toJSONString(jobPo));
                }
            }
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                FIXED_EXECUTOR_SERVICE.shutdown();
            }
            LOGGER.info("Executable dead job checker stopped!");
        } catch (Throwable t) {
            LOGGER.error("Executable dead job checker stop failed!", t);
        }
    }
}
