package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.commons.utils.*;
import com.tyyd.framework.dat.core.commons.utils.Callable;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.support.NodeShutdownHook;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractPreLoader implements PreLoader {

	private int loadSize;
	// 预取阀值
	private double factor;

	private JobPriorityBlockingQueue queue = null;
	private ScheduledExecutorService LOAD_EXECUTOR_SERVICE = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory("LTS-PreLoader", true));
	private ScheduledFuture<?> scheduledFuture;
	private AtomicBoolean start = new AtomicBoolean(false);

	public AbstractPreLoader(final AppContext appContext) {
		if (start.compareAndSet(false, true)) {
			loadSize = appContext.getConfig().getParameter("task.preloader.size", 300);
			factor = appContext.getConfig().getParameter("task.preloader.factor", 0.2);
			queue = new JobPriorityBlockingQueue(loadSize);
			scheduledFuture = LOAD_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					if (queue.size() / loadSize < factor) {
						// load
						List<TaskPo> loads = load(loadSize - queue.size());
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
				}
			}, 500, 500, TimeUnit.MILLISECONDS);

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

	public TaskPo take(String taskExecuterIdentity) {
		TaskPo taskPo = queue.poll();
		return taskPo;
	}

	/**
	 * 锁定任务
	 */
	public abstract boolean lockTask(String id, String taskTrackerIdentity, Long triggerTime, Long gmtModified);

	/**
	 * 加载任务
	 */
	protected abstract List<TaskPo> load(int loadSize);
}
