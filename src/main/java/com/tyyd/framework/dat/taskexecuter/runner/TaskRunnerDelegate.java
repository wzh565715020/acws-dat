package com.tyyd.framework.dat.taskexecuter.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.domain.Response;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;
import com.tyyd.framework.dat.taskexecuter.monitor.TaskExecuterMStatReporter;

import sun.nio.ch.Interruptible;

/**
 * TaskRunner 的代理类, 1. 做一些错误处理之类的 2. 监控统计 3. Context信息设置
 *
 */
public class TaskRunnerDelegate implements Runnable {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunnerDelegate.class);
	private TaskMeta taskMeta;
	private RunnerCallback callback;
	private TaskExecuterAppContext appContext;
	private TaskExecuterMStatReporter stat;
	private Interruptible interruptor;
	private TaskRunner curTaskRunner;
	private AtomicBoolean interrupted = new AtomicBoolean(false);
	private Thread thread;

	public TaskRunnerDelegate(TaskExecuterAppContext appContext, TaskMeta taskMeta, RunnerCallback callback) {
		this.appContext = appContext;
		this.callback = callback;
		this.taskMeta = taskMeta;
		stat = (TaskExecuterMStatReporter) appContext.getMStatReporter();

		this.interruptor = new InterruptibleAdapter() {
			public void interrupt() {
				TaskRunnerDelegate.this.interrupt();
			}
		};
	}

	@Override
	public void run() {

		thread = Thread.currentThread();

		try {
			blockedOn(interruptor);
			if (Thread.currentThread().isInterrupted()) {
				((InterruptibleAdapter) interruptor).interrupt();
			}
			long startTime = SystemClock.now();
			// 设置当前context中的jobId
			Response response = new Response();
			response.setTaskMeta(taskMeta);
			try {
				appContext.getRunnerPool().getRunningTaskManager().in(taskMeta.getId(), this);
				
				this.curTaskRunner = appContext.getRunnerFactory().newRunner(taskMeta.getTask().getTaskClass());
				
				Result result = this.curTaskRunner.run(taskMeta.getTask());

				if (result == null) {
					response.setAction(Action.EXECUTE_SUCCESS);
				} else {
					if (result.getAction() == null) {
						response.setAction(Action.EXECUTE_SUCCESS);
					} else {
						response.setAction(result.getAction());
					}
					response.setMsg(result.getMsg());
				}

				long time = SystemClock.now() - startTime;
				stat.addRunningTime(time);
				LOGGER.info("task execute completed : {}, time:{} ms.", taskMeta, time);
			} catch (Throwable t) {
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				response.setAction(Action.EXECUTE_EXCEPTION);
				response.setMsg(sw.toString());
				long time = SystemClock.now() - startTime;
				stat.addRunningTime(time);
				LOGGER.info("task execute error : {}, time: {}, {}", taskMeta, time, t.getMessage(), t);
			} finally {
				checkInterrupted();
				appContext.getRunnerPool().getRunningTaskManager().out(taskMeta.getId());
			}
			// 统计数据
			stat(response.getAction());
			callback.runComplete(response);

		} finally {
			blockedOn(null);
		}
	}

	private void interrupt() {
		if (!interrupted.compareAndSet(false, true)) {
			return;
		}
		if (this.curTaskRunner != null && this.curTaskRunner instanceof InterruptibleTaskRunner) {
			((InterruptibleTaskRunner) this.curTaskRunner).interrupt();
		}
	}

	private boolean isInterrupted() {
		return this.interrupted.get();
	}

	private void stat(Action action) {
		if (action == null) {
			return;
		}
		switch (action) {
		case EXECUTE_SUCCESS:
			stat.incSuccessNum();
			break;
		case EXECUTE_FAILED:
			stat.incFailedNum();
			break;
		case EXECUTE_LATER:
			stat.incExeLaterNum();
			break;
		case EXECUTE_EXCEPTION:
			stat.incExeExceptionNum();
			break;
		}
	}

	private static void blockedOn(Interruptible interruptible) {
		sun.misc.SharedSecrets.getJavaLangAccess().blockedOn(Thread.currentThread(), interruptible);
	}

	private abstract class InterruptibleAdapter implements Interruptible {
		// for > jdk7
		public void interrupt(Thread thread) {
			interrupt();
		}

		public abstract void interrupt();
	}

	private void checkInterrupted() {
		try {
			if (isInterrupted()) {
				LOGGER.info("SYSTEM:Interrupted");
			}
		} catch (Throwable t) {
			LOGGER.warn("checkInterrupted error", t);
		}
	}

	public Thread currentThread() {
		return thread;
	}

	public TaskMeta currentJob() {
		return taskMeta;
	}
}
