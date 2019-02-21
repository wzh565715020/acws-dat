package com.tyyd.framework.dat.taskdispatch.support.checker;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.core.cluster.Node;
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
						.getParameter("TaskDispatcher.task.pool.check.interval.seconds", 5);
				scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						checkAndDistribute();
					}
				}, checkPeriodSeconds, checkPeriodSeconds, TimeUnit.SECONDS);
			}
			LOGGER.info("Executing task pool checker started!");
		} catch (Throwable e) {
			LOGGER.error("Executing dead task checker start failed!", e);
		}
	}

	private void checkAndDistribute() {
		PoolQueueReq request = new PoolQueueReq();
		request.setLimit(Integer.MAX_VALUE);
		PaginationRsp<PoolPo> paginationRsp = appContext.getPoolQueue().pageSelect(request);
		if (paginationRsp != null && paginationRsp.getRows() != null && !paginationRsp.getRows().isEmpty()) {
			List<PoolPo> list = paginationRsp.getRows();
			for (PoolPo poolPo : list) {
				if (poolPo.getNodeId() != null && !poolPo.getNodeId().equals("")
						&& !appContext.getTaskDispatcherManager().containNode(poolPo.getNodeId())) {
					Node node = new Node();
					node.setIdentity(poolPo.getNodeId());
					appContext.getTaskDispatcherManager().addRemoveTaskDispatcher(node);
				}
			}
		}
		appContext.getTaskDispatcherManager().removeTaskDispatcher();
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
