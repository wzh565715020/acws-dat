package com.tyyd.framework.dat.taskdispatch.support.checker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskPoolChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskPoolChecker.class);

	private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("DAT-TaskPool-Checker", true));

	private TaskDispatcherAppContext appContext;

	public TaskPoolChecker(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	private AtomicBoolean start = new AtomicBoolean(false);
	private ScheduledFuture<?> scheduledFuture;

	public void start() {
		try {
			if (start.compareAndSet(false, true)) {
				int checkPeriodSeconds = appContext.getConfig()
						.getParameter("TaskDispatcher.task.pool.check.interval.seconds", 30);
				if (checkPeriodSeconds < 5) {
					checkPeriodSeconds = 5;
				} else if (checkPeriodSeconds > 5 * 60) {
					checkPeriodSeconds = 5 * 60;
				}
				scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
							checkAndDistribute();
					}
				}, checkPeriodSeconds, checkPeriodSeconds, TimeUnit.SECONDS);
			}
			LOGGER.info("Executing dead task checker started!");
		} catch (Throwable e) {
			LOGGER.error("Executing dead task checker start failed!", e);
		}
	}

	private void checkAndDistribute() {
		List<PoolPo> poolPos = appContext.getPoolQueue().getUndistributedPool();
		if (poolPos == null || poolPos.isEmpty()) {
			return;
		}
		appContext.getTaskDispatcherManager().poolChangeRedistribution();
	}

	public void stop() {
		try {
			if (start.compareAndSet(true, false)) {
				scheduledFuture.cancel(true);
				FIXED_EXECUTOR_SERVICE.shutdown();
			}
			LOGGER.info("Executing dead task checker stopped!");
		} catch (Throwable t) {
			LOGGER.error("Executing dead task checker stop failed!", t);
		}
	}

}
