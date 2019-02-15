package com.tyyd.framework.dat.taskdispatch.support;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskPushMachine {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskPushMachine.class.getSimpleName());

	private AtomicBoolean start = new AtomicBoolean(false);

	private TaskDispatcherAppContext appContext;

	private List<TaskPusher> taskPushers = new CopyOnWriteArrayList<>();

	private Set<String> runningPoolIdSet = new ConcurrentHashSet<String>();

	private Runnable worker;

	private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("dat-TaskPushMachine-Executor", true));

	private ScheduledFuture<?> scheduledFuture;

	public TaskPushMachine(final TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
		this.worker = new Runnable() {
			@Override
			public void run() {
				initTaskPushers();
			}
		};
	}

	public void start() {
		try {
			if (!start.compareAndSet(false, true)) {
				return;
			}
			if (scheduledFuture == null) {
				scheduledFuture = SCHEDULED_CHECKER.scheduleWithFixedDelay(worker, 1,
						Constants.DEFAULT_TASK_PUSH_FREQUENCY, TimeUnit.SECONDS);
			}
		} catch (Throwable t) {
			LOGGER.error("Start stask push machine failed!", t);
		}
	}

	public void initTaskPushers() {
		List<PoolPo> poolPos = appContext.getPoolQueue().getPoolByNodeId(appContext.getConfig().getIdentity());
		if ((poolPos == null || poolPos.isEmpty())) {
			if (taskPushers != null && !taskPushers.isEmpty()) {
				for (TaskPusher taskPusher : taskPushers) {
					taskPusher.stop();
				}
			}
			return;
		}

		Set<String> poolIdSet = new HashSet<String>();
		for (PoolPo poolPo : poolPos) {
			poolIdSet.add(poolPo.getPoolId());
			if (runningPoolIdSet.contains(poolPo.getPoolId())) {
				TaskPusher taskPusher = getTaskPusher(poolPo.getPoolId());
				if (taskPusher != null && !taskPusher.getStart().get()) {
					taskPusher.start();
				}
			} else {
				runningPoolIdSet.add(poolPo.getPoolId());
				TaskPusher taskPusher = new TaskPusher(appContext, poolPo.getPoolId());
				taskPushers.add(taskPusher);
				taskPusher.start();
			}
		}
		for (String runningPoolId : runningPoolIdSet) {
			if (!poolIdSet.contains(runningPoolId)) {
				TaskPusher taskPusher = getTaskPusher(runningPoolId);
				if (taskPusher != null && taskPusher.getStart().get()) {
					taskPusher.stop();
				}
			}
		}

	}

	public void stop() {
		if (taskPushers.isEmpty()) {
			return;
		}
		for (TaskPusher taskPusher : taskPushers) {
			taskPusher.stop();
		}
	}

	private TaskPusher getTaskPusher(String poolId) {
		for (TaskPusher taskPusher : taskPushers) {
			if (poolId != null && poolId.equals(taskPusher.getPoolId())) {
				return taskPusher;
			}
		}
		return null;
	}
}
