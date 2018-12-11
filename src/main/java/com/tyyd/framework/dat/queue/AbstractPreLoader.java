package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.commons.utils.*;
import com.tyyd.framework.dat.core.commons.utils.Callable;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.support.NodeShutdownHook;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public abstract class AbstractPreLoader implements PreLoader {

	private int loadSize;
	// 预取阀值
	private double factor;

	private ConcurrentHashMap<String/* taskTrackerNodeGroup */, JobPriorityBlockingQueue> JOB_MAP = new ConcurrentHashMap<String, JobPriorityBlockingQueue>();

	// 加载的信号
	private ConcurrentHashSet<String> LOAD_SIGNAL = new ConcurrentHashSet<String>();

	private ScheduledExecutorService LOAD_EXECUTOR_SERVICE = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory("LTS-PreLoader", true));
	private ScheduledFuture<?> scheduledFuture;
	private AtomicBoolean start = new AtomicBoolean(false);
	private String FORCE_PREFIX = "force_"; // 强制加载的信号

	public AbstractPreLoader(final AppContext appContext) {
		if (start.compareAndSet(false, true)) {

			loadSize = appContext.getConfig().getParameter("job.preloader.size", 300);
			factor = appContext.getConfig().getParameter("job.preloader.factor", 0.2);

			scheduledFuture = LOAD_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {

					for (String loadTaskTrackerNodeGroup : LOAD_SIGNAL) {

						// 是否是强制加载
						boolean force = false;
						if (loadTaskTrackerNodeGroup.startsWith(FORCE_PREFIX)) {
							loadTaskTrackerNodeGroup = loadTaskTrackerNodeGroup.replaceFirst(FORCE_PREFIX, "");
							force = true;
						}

						JobPriorityBlockingQueue queue = JOB_MAP.get(loadTaskTrackerNodeGroup);
						if (!force && queue.size() / loadSize < factor) {
							// load
							List<TaskPo> loads = load(loadTaskTrackerNodeGroup, loadSize - queue.size());
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
						LOAD_SIGNAL.remove(loadTaskTrackerNodeGroup);
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

	public TaskPo take(String taskTrackerNodeGroup, String taskTrackerIdentity) {
		while (true) {
			TaskPo taskPo = get(taskTrackerNodeGroup);
			if (taskPo == null) {
				return null;
			}
			// update jobPo
			if (lockJob(taskTrackerNodeGroup, taskPo.getTaskId(), taskTrackerIdentity, taskPo.getTriggerTime(),
					taskPo.getGmtModified())) {
				taskPo.setTaskTrackerIdentity(taskTrackerIdentity);
				taskPo.setIsRunning(true);
				taskPo.setGmtModified(SystemClock.now());
				return taskPo;
			}
		}
	}

	@Override
	public void load(String taskTrackerNodeGroup) {
		if (StringUtils.isEmpty(taskTrackerNodeGroup)) {
			for (String key : JOB_MAP.keySet()) {
				LOAD_SIGNAL.add(FORCE_PREFIX + key);
			}
			return;
		}
		LOAD_SIGNAL.add(FORCE_PREFIX + taskTrackerNodeGroup);
	}

	/**
	 * 锁定任务
	 */
	protected abstract boolean lockJob(String taskTrackerNodeGroup, String jobId, String taskTrackerIdentity,
			Long triggerTime, Long gmtModified);

	/**
	 * 加载任务
	 */
	protected abstract List<TaskPo> load(String loadTaskTrackerNodeGroup, int loadSize);

	private TaskPo get(String taskExecuterNodeGroup) {
		JobPriorityBlockingQueue queue = JOB_MAP.get(taskExecuterNodeGroup);
		if (queue == null) {
			queue = new JobPriorityBlockingQueue(loadSize);
			JobPriorityBlockingQueue oldQueue = JOB_MAP.putIfAbsent(taskExecuterNodeGroup, queue);
			if (oldQueue != null) {
				queue = oldQueue;
			}
		}

		if (queue.size() / loadSize < factor) {
			// 触发加载的请求
			if (!LOAD_SIGNAL.contains(taskExecuterNodeGroup)) {
				LOAD_SIGNAL.add(taskExecuterNodeGroup);
			}
		}
		return queue.poll();
	}

}
