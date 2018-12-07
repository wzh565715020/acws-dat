package com.tyyd.framework.dat.taskexecuter.monitor;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.ec.EventInfo;
import com.tyyd.framework.dat.ec.EventSubscriber;
import com.tyyd.framework.dat.ec.Observer;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

/**
 * 当TaskTracker和JobTracker断开超过了一段时间，TaskTracker立即停止当前的所有任务
 *
 */
public class StopWorkingMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StopWorkingMonitor.class);
    private TaskExecuterAppContext appContext;
    private AtomicBoolean start = new AtomicBoolean(false);
    private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-StopWorking-Monitor", true));
    private ScheduledFuture<?> scheduledFuture;
    private String ecSubscriberName = StopWorkingMonitor.class.getSimpleName();
    private EventSubscriber eventSubscriber;
    private Long offlineTimestamp = null;

    public StopWorkingMonitor(TaskExecuterAppContext appContext) {
        this.appContext = appContext;
    }

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                eventSubscriber = new EventSubscriber(ecSubscriberName, new Observer() {
                    @Override
                    public void onObserved(EventInfo eventInfo) {
                        // 当JobTracker可用的时候，置为null, 重新统计
                        offlineTimestamp = null;
                    }
                });
                appContext.getEventCenter().subscribe(eventSubscriber, EcTopic.JOB_TRACKER_AVAILABLE);

                scheduledFuture = SCHEDULED_CHECKER.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (offlineTimestamp == null && appContext.getRemotingClient().isServerEnable()) {
                                offlineTimestamp = SystemClock.now();
                            }

                            if (offlineTimestamp != null &&
                                    SystemClock.now() - offlineTimestamp > Constants.DEFAULT_TASK_EXECUTER_OFFLINE_LIMIT_MILLIS) {
                                // 停止所有任务
                                appContext.getRunnerPool().stopWorking();
                                offlineTimestamp = null;
                            }
                        } catch (Throwable t) {
                            LOGGER.error("Check ", t);
                        }
                    }
                }, 3, 3, TimeUnit.SECONDS);
                LOGGER.info("start succeed ");
            }
        } catch (Throwable t) {
            LOGGER.error("start failed ", t);
        }
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                SCHEDULED_CHECKER.shutdown();

                appContext.getEventCenter().unSubscribe(EcTopic.JOB_TRACKER_AVAILABLE, eventSubscriber);

                LOGGER.info("stop succeed ");
            }
        } catch (Throwable t) {
            LOGGER.error("stop failed ", t);
        }
    }

}
