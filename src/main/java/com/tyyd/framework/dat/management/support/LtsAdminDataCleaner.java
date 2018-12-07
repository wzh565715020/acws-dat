package com.tyyd.framework.dat.management.support;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.tyyd.framework.dat.core.commons.utils.Callable;
import com.tyyd.framework.dat.core.commons.utils.DateUtils;
import com.tyyd.framework.dat.core.commons.utils.QuietUtils;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.request.JvmDataReq;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;
import com.tyyd.framework.dat.management.request.NodeOnOfflineLogPaginationReq;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 定时清除 monitor 数据
 *
 */
public class LtsAdminDataCleaner implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(LtsAdminDataCleaner.class);

    @Autowired
    private BackendAppContext appContext;

    private ScheduledExecutorService cleanExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("LTS-Admin-Clean", true));

    private AtomicBoolean start = new AtomicBoolean(false);

    public void start() {
        if (start.compareAndSet(false, true)) {
            cleanExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        clean();
                    } catch (Throwable t) {
                        LOGGER.error("Clean monitor data error ", t);
                    }
                }
            }, 1, 24, TimeUnit.HOURS);
            LOGGER.info("LtsAdminDataCleaner start succeed ");
        }
    }

    private void clean() {
        //  1. 清除TaskTracker JobTracker, JobClient的统计数据(3天之前的)
        final MDataPaginationReq request = new MDataPaginationReq();
        request.setEndTime(DateUtils.addDay(new Date(), -3).getTime());

        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendTaskTrackerMAccess().delete(request);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJobTrackerMAccess().delete(request);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJobClientMAccess().delete(request);
            }
        });

        // 2. 清除30天以前的节点上下线日志
        final NodeOnOfflineLogPaginationReq nodeOnOfflineLogPaginationReq = new NodeOnOfflineLogPaginationReq();
        nodeOnOfflineLogPaginationReq.setEndLogTime(DateUtils.addDay(new Date(), -30));

        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendNodeOnOfflineLogAccess().delete(nodeOnOfflineLogPaginationReq);
            }
        });

        // 3. 清除3天前的JVM监控信息
        final JvmDataReq jvmDataReq = new JvmDataReq();
        jvmDataReq.setEndTime(DateUtils.addDay(new Date(), -3).getTime());
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJVMGCAccess().delete(jvmDataReq);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJVMThreadAccess().delete(jvmDataReq);
            }
        });
        QuietUtils.doWithWarn(new Callable() {
            @Override
            public void call() throws Exception {
                appContext.getBackendJVMMemoryAccess().delete(jvmDataReq);
            }
        });

        LOGGER.info("Clean monitor data succeed ");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }
}