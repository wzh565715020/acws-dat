package com.tyyd.framework.dat.taskexecuter.runner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.ec.EventInfo;
import com.tyyd.framework.dat.ec.EventSubscriber;
import com.tyyd.framework.dat.ec.Observer;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;
import com.tyyd.framework.dat.taskexecuter.expcetion.NoAvailableTaskRunnerException;

/**
 * 线程池管理
 */
public class RunnerPool {

	private final Logger LOGGER = LoggerFactory.getLogger("DAT.RunnerPool");

	private ThreadPoolExecutor threadPoolExecutor = null;

	private RunnerFactory runnerFactory;
	private TaskExecuterAppContext appContext;
	private RunningTaskManager runningTaskManager;

	public RunnerPool(final TaskExecuterAppContext appContext) {
		this.appContext = appContext;
		this.runningTaskManager = new RunningTaskManager();

		threadPoolExecutor = initThreadPoolExecutor();

		runnerFactory = appContext.getRunnerFactory();
		if (runnerFactory == null) {
			runnerFactory = new DefaultRunnerFactory(appContext);
		}
		// 向事件中心注册事件, 改变工作线程大小
		appContext.getEventCenter().subscribe(new EventSubscriber(appContext.getConfig().getIdentity(), new Observer() {
			@Override
			public void onObserved(EventInfo eventInfo) {
				setWorkThread(appContext.getConfig().getWorkThreads());
			}
		}), EcTopic.WORK_THREAD_CHANGE);
	}

	private ThreadPoolExecutor initThreadPoolExecutor() {
		int workThreads = appContext.getConfig().getWorkThreads();

		return new ThreadPoolExecutor(workThreads, workThreads, 30, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), // 直接提交给线程而不保持它们
				new ThreadPoolExecutor.AbortPolicy());
	}

	public void execute(TaskMeta jobMeta, RunnerCallback callback)
			throws NoAvailableTaskRunnerException {
		try {
			threadPoolExecutor.execute(new TaskRunnerDelegate(appContext, jobMeta, callback));
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Receive task success ! " + jobMeta);
			}
		} catch (RejectedExecutionException e) {
			LOGGER.warn("No more thread to run task .");
			throw new NoAvailableTaskRunnerException(e);
		}
	}

	/**
	 * 得到当前可用的线程数
	 */
	public int getAvailablePoolSize() {
		return threadPoolExecutor.getMaximumPoolSize() - threadPoolExecutor.getActiveCount();
	}

	public void setWorkThread(int workThread) {
		if (workThread == 0) {
			throw new IllegalArgumentException("workThread can not be zero!");
		}

		threadPoolExecutor.setMaximumPoolSize(workThread);
		threadPoolExecutor.setCorePoolSize(workThread);

		LOGGER.info("workThread update to {}", workThread);
	}

	/**
	 * 得到最大线程数
	 */
	public int getWorkThread() {
		return threadPoolExecutor.getCorePoolSize();
	}

	public RunnerFactory getRunnerFactory() {
		return runnerFactory;
	}

	/**
	 * 执行该方法，线程池的状态立刻变成STOP状态，并试图停止所有正在执行的线程，不再处理还在池队列中等待的任务，当然，它会返回那些未执行的任务。
	 * 它试图终止线程的方法是通过调用Thread.interrupt()方法来实现的，但是大家知道，这种方法的作用有限， 如果线程中没有sleep
	 * 、wait、Condition、定时锁等应用, interrupt()方法是无法中断当前的线程的。
	 * 所以，ShutdownNow()并不代表线程池就一定立即就能退出，它可能必须要等待所有正在执行的任务都执行完成了才能退出。
	 * 特殊的时候可以通过使用{@link InterruptibleJobRunner}来解决
	 */
	public void stopWorking() {
		try {
			threadPoolExecutor.shutdownNow();
			Thread.sleep(1000);
			threadPoolExecutor = initThreadPoolExecutor();
			LOGGER.info("stop working succeed ");
		} catch (Throwable t) {
			LOGGER.error("stop working failed ", t);
		}
	}

	public void shutDown() {
		try {
			threadPoolExecutor.shutdownNow();
			LOGGER.info("stop working succeed ");
		} catch (Throwable t) {
			LOGGER.error("stop working failed ", t);
		}
	}

	/**
	 * 用来管理正在执行的任务
	 */
	public class RunningTaskManager {

		private final ConcurrentMap<String, TaskRunnerDelegate> TASKS = new ConcurrentHashMap<String, TaskRunnerDelegate>();

		public void in(String id, TaskRunnerDelegate taskRunnerDelegate) {
			TASKS.putIfAbsent(id, taskRunnerDelegate);
		}

		public void out(String id) {
			TASKS.remove(id);
		}

		public boolean running(String id) {
			return TASKS.containsKey(id);
		}

		/**
		 * 返回给定list中不存在的jobId
		 */
		public List<String> getNotExists(List<String> ids) {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Ask tasks: " + ids + " Running tasks ：" + TASKS.keySet());
			}
			List<String> notExistList = new ArrayList<String>();
			for (String id : ids) {
				if (!running(id)) {
					notExistList.add(id);
				}
			}
			return notExistList;
		}

		public void terminateJob(String jobId) {
			TaskRunnerDelegate taskRunnerDelegate = TASKS.get(jobId);
			if (taskRunnerDelegate != null) {
				try {
					taskRunnerDelegate.currentThread().interrupt();
				} catch (Throwable e) {
					LOGGER.error("terminateJob [" + jobId + "]  error", e);
				}
			}
		}
	}

	public RunningTaskManager getRunningTaskManager() {
		return runningTaskManager;
	}
}
