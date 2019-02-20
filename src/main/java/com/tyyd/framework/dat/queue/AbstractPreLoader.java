package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.commons.utils.Callable;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.support.NodeShutdownHook;
import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractPreLoader implements PreLoader {

	private final Logger LOGGER = LoggerFactory.getLogger(AbstractPreLoader.class);
	
	private int loadSize;
	// 预取阀值
	private double factor;
	
	private String poolId;

	private TaskPriorityBlockingQueue queue = null;
	
	private ScheduledExecutorService LOAD_EXECUTOR_SERVICE = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory("DAT-PreLoader", true));
	
	private ScheduledFuture<?> scheduledFuture;
	
	private AtomicBoolean start = new AtomicBoolean(false);

	public AbstractPreLoader(final AppContext appContext, String poolId) {
		if (start.compareAndSet(false, true)) {
			loadSize = appContext.getConfig().getParameter("task.preloader.size", 300);
			factor = appContext.getConfig().getParameter("task.preloader.factor", 0.2);
			queue = new TaskPriorityBlockingQueue(loadSize);
			this.poolId = poolId;
			scheduledFuture = LOAD_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					LOGGER.info("线程池" + poolId + "获取任务开始");
					if (queue.size() / loadSize < factor) {
						// load
						List<TaskPo> loads = load(poolId, loadSize - queue.size());
						// 加入到内存中
						if (CollectionUtils.isNotEmpty(loads)) {
							for (TaskPo load : loads) {
								if (!queue.offer(load)) {
									// 没有成功说明已经满了
									break;
								}
							}
						}
					}
					LOGGER.info("线程池" + poolId + "获取任务结束");
				}
			}, 5000, 5000, TimeUnit.MILLISECONDS);

			NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new Callable() {
				@Override
				public void call() throws Exception {
					scheduledFuture.cancel(true);
					LOAD_EXECUTOR_SERVICE.shutdown();
					start.set(false);
				}
			});
		}
	}

	public void stop() {
		if(start.compareAndSet(true, false)) {
			queue.removeAll();
			scheduledFuture.cancel(false);
			LOAD_EXECUTOR_SERVICE.shutdown();
		}
	}

	public TaskPo take() {
		return queue.poll();
	}
	
    @Override
	public String getPoolId() {
		return poolId;
	}

	/**
	 * 锁定任务
	 */
	public abstract boolean lockTask(String id, String taskTrackerIdentity);

	/**
	 * 加载任务
	 */
	protected abstract List<TaskPo> load(String poolId, int loadSize);
}
