package com.tyyd.framework.dat.biz.logger;

import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.JobLoggerRequest;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.NodeShutdownHook;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内部根据用户参数决定是否采用延迟批量刷盘的策略,来提高吞吐量
 * 批量刷盘有两种情况:
 * 1. 内存的日志量超过了设置的阀值
 * 2. 每3S检查一次内存中是否有日志,如果有就那么刷盘
 *
 * @author   on 10/2/15.
 */
public class LazyJobLogger implements TaskLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartJobLogger.class);
    private TaskLogger delegate;
    // 无界Queue
    private BlockingQueue<TaskLogPo> memoryQueue = new LinkedBlockingQueue<TaskLogPo>();
    // 日志批量刷盘数量
    private int batchFlushSize;
    private int overflowSize;
    // 内存中最大的日志量阀值
    private int maxMemoryLogSize;
    private AtomicBoolean flushing = new AtomicBoolean(false);

    public LazyJobLogger(AppContext appContext, TaskLogger delegate) {
        this.delegate = delegate;

        Config config = appContext.getConfig();
        maxMemoryLogSize = config.getParameter(Constants.LAZY_TASK_LOGGER_MEM_SIZE, 1000);
        int flushPeriod = config.getParameter(Constants.LAZY_TASK_LOGGER_CHECK_PERIOD, 3);
        batchFlushSize = config.getParameter(Constants.LAZY_TASK_LOGGER_BATCH_FLUSH_SIZE, 100);
        overflowSize = config.getParameter(Constants.LAZY_TASK_LOGGER_OVERFLOW_SIZE, 10000);

        final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory("LazyJobLogger", true));
        final ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    if (flushing.compareAndSet(false, true)) {
                        checkAndFlush();
                    }
                } catch (Throwable t) {
                    LOGGER.error("CheckAndFlush log error", t);
                }
            }
        }, flushPeriod, flushPeriod, TimeUnit.SECONDS);

        NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new com.tyyd.framework.dat.core.commons.utils.Callable() {
            public void call() throws Exception {
                scheduledFuture.cancel(true);
                executor.shutdownNow();
            }
        });
    }

    /**
     * 检查内存中是否有日志,如果有就批量刷盘
     */
    private void checkAndFlush() {
        try {
            int nowSize = memoryQueue.size();
            if (nowSize == 0) {
                return;
            }
            List<TaskLogPo> batch = new ArrayList<TaskLogPo>();
            for (int i = 0; i < nowSize; i++) {
                TaskLogPo jobLogPo = memoryQueue.poll();
                batch.add(jobLogPo);

                if (batch.size() >= batchFlushSize) {
                    flush(batch);
                }
            }
            if (batch.size() > 0) {
                flush(batch);
            }

        } finally {
            flushing.compareAndSet(true, false);
        }
    }

    private void checkOverflowSize() {
        if (memoryQueue.size() > overflowSize) {
            throw new JobLogException("Memory Log size is " +
                    memoryQueue.size() + " , please check the JobLogger is available");
        }
    }

    private void flush(List<TaskLogPo> batch) {
        boolean flushSuccess = false;
        try {
            delegate.log(batch);
            flushSuccess = true;
        } finally {
            if (!flushSuccess) {
                memoryQueue.addAll(batch);
            }
            batch.clear();
        }
    }

    /**
     * 检查内存中的日志量是否超过阀值,如果超过需要批量刷盘日志
     */
    private void checkCapacity() {
        if (memoryQueue.size() > maxMemoryLogSize) {
            // 超过阀值,需要批量刷盘
            if (flushing.compareAndSet(false, true)) {
                // 这里可以采用new Thread, 因为这里只会同时new一个
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            checkAndFlush();
                        } catch (Throwable t) {
                            LOGGER.error("Capacity full flush error", t);
                        }
                    }
                }).start();
            }
        }
    }

    @Override
    public void log(TaskLogPo jobLogPo) {
        if (jobLogPo == null) {
            return;
        }
        checkOverflowSize();
        memoryQueue.offer(jobLogPo);
        checkCapacity();
    }

    @Override
    public void log(List<TaskLogPo> jobLogPos) {
        if (CollectionUtils.isEmpty(jobLogPos)) {
            return;
        }
        checkOverflowSize();
        for (TaskLogPo jobLogPo : jobLogPos) {
            memoryQueue.offer(jobLogPo);
        }
        // checkCapacity
        checkCapacity();
    }

    @Override
    public PaginationRsp<TaskLogPo> search(JobLoggerRequest request) {
        return delegate.search(request);
    }

}
