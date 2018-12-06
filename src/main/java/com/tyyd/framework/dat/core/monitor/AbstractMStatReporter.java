package com.tyyd.framework.dat.core.monitor;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.domain.monitor.MData;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.jvmmonitor.JVMMonitor;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/30/15.
 */
public abstract class AbstractMStatReporter implements MStatReporter {

    protected final Logger LOGGER = LoggerFactory.getLogger(AbstractMStatReporter.class);

    protected AppContext appContext;
    protected Config config;

    private ScheduledExecutorService executor = Executors
            .newSingleThreadScheduledExecutor(new NamedThreadFactory("LTS-Monitor-data-collector", true));
    private ScheduledFuture<?> scheduledFuture;
    private AtomicBoolean start = new AtomicBoolean(false);

    public AbstractMStatReporter(AppContext appContext) {
        this.appContext = appContext;
        this.config = appContext.getConfig();
    }

    public final void start() {

        // 启动JVM监控
        JVMMonitor.start();

        try {
            if (start.compareAndSet(false, true)) {
                scheduledFuture = executor.scheduleWithFixedDelay(
                        new MStatReportWorker(appContext, this), 1, 1, TimeUnit.SECONDS);
                LOGGER.info("MStatReporter start succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("MStatReporter start failed.", e);
        }
    }

    /**
     * 用来收集数据
     */
    protected abstract MData collectMData();

    protected abstract NodeType getNodeType();

    public final void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                executor.shutdown();
                JVMMonitor.stop();
                LOGGER.info("MStatReporter stop succeed.");
            }
        } catch (Exception e) {
            LOGGER.error("MStatReporter stop failed.", e);
        }
    }

}
