package com.tyyd.framework.dat.taskexecuter.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.domain.Response;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;
import com.tyyd.framework.dat.taskexecuter.logger.BizLoggerAdapter;
import com.tyyd.framework.dat.taskexecuter.logger.BizLoggerFactory;
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
	private BizLoggerAdapter logger;
	private TaskExecuterAppContext appContext;
	private TaskExecuterMStatReporter stat;
	private Interruptible interruptor;
	private TaskRunner curTaskRunner;
	private AtomicBoolean interrupted = new AtomicBoolean(false);
	private Thread thread;
	private Channel channel;

	public TaskRunnerDelegate(TaskExecuterAppContext appContext, TaskMeta taskMeta, RunnerCallback callback,
			Channel channel) {
		this.appContext = appContext;
		this.callback = callback;
		this.taskMeta = taskMeta;
        this.channel = channel;
		this.logger = (BizLoggerAdapter) BizLoggerFactory.getLogger(appContext.getBizLogLevel(),
				appContext.getRemotingServer(), appContext);
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

			DatLoggerFactory.setLogger(logger);

			long startTime = SystemClock.now();
			// 设置当前context中的jobId
			logger.setId(taskMeta.getId(), taskMeta.getTask().getTaskId());
			Response response = new Response();
			response.setJobMeta(taskMeta);
			response.setChannel(this.channel);
			try {
				appContext.getRunnerPool().getRunningTaskManager().in(taskMeta.getId(), this);
				this.curTaskRunner = appContext.getRunnerPool().getRunnerFactory().newRunner();
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
				LOGGER.info("Job execute completed : {}, time:{} ms.", taskMeta.getTask(), time);
			} catch (Throwable t) {
				StringWriter sw = new StringWriter();
				t.printStackTrace(new PrintWriter(sw));
				response.setAction(Action.EXECUTE_EXCEPTION);
				response.setMsg(sw.toString());
				long time = SystemClock.now() - startTime;
				stat.addRunningTime(time);
				LOGGER.info("Job execute error : {}, time: {}, {}", taskMeta.getTask(), time, t.getMessage(), t);
			} finally {
				checkInterrupted();
				logger.removeId();
				appContext.getRunnerPool().getRunningTaskManager().out(taskMeta.getId());
			}
			// 统计数据
			stat(response.getAction());
			callback.runComplete(response);

		} finally {
			DatLoggerFactory.remove();

			blockedOn(null);
		}
	}

	private void interrupt() {
		if (!interrupted.compareAndSet(false, true)) {
			return;
		}
		if (this.curTaskRunner != null && this.curTaskRunner instanceof InterruptibleJobRunner) {
			((InterruptibleJobRunner) this.curTaskRunner).interrupt();
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

	private boolean isStopToGetNewJob() {
		if (isInterrupted()) {
			// 如果当前线程被阻断了,那么也就不接受新任务了
			return true;
		}
		// 机器资源是否充足
		return !appContext.getConfig().getInternalData(Constants.MACHINE_RES_ENOUGH, true);
	}

	private void checkInterrupted() {
		try {
			if (isInterrupted()) {
				logger.info("SYSTEM:Interrupted");
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
