package com.tyyd.framework.dat.taskexecuter.runner;


import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.JobMeta;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.domain.Response;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;
import com.tyyd.framework.dat.taskexecuter.logger.BizLoggerAdapter;
import com.tyyd.framework.dat.taskexecuter.logger.BizLoggerFactory;
import com.tyyd.framework.dat.taskexecuter.monitor.TaskExecuterMStatReporter;

import sun.nio.ch.Interruptible;

/**
 * Job Runner 的代理类,
 * 1. 做一些错误处理之类的
 * 2. 监控统计
 * 3. Context信息设置
 *
 */
public class TaskRunnerDelegate implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRunnerDelegate.class);
    private JobMeta jobMeta;
    private RunnerCallback callback;
    private BizLoggerAdapter logger;
    private TaskExecuterAppContext appContext;
    private TaskExecuterMStatReporter stat;
    private Interruptible interruptor;
    private TaskRunner curJobRunner;
    private AtomicBoolean interrupted = new AtomicBoolean(false);
    private Thread thread;

    public TaskRunnerDelegate(TaskExecuterAppContext appContext,
                             JobMeta jobMeta, RunnerCallback callback) {
        this.appContext = appContext;
        this.callback = callback;
        this.jobMeta = jobMeta;

        this.logger = (BizLoggerAdapter) BizLoggerFactory.getLogger(
                appContext.getBizLogLevel(),
                appContext.getRemotingClient(), appContext);
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

            DtaLoggerFactory.setLogger(logger);

            while (jobMeta != null) {
                long startTime = SystemClock.now();
                // 设置当前context中的jobId
                logger.setId(jobMeta.getJobId(), jobMeta.getJob().getTaskId());
                Response response = new Response();
                response.setJobMeta(jobMeta);
                try {
                    appContext.getRunnerPool().getRunningJobManager()
                            .in(jobMeta.getJobId(), this);
                    this.curJobRunner = appContext.getRunnerPool().getRunnerFactory().newRunner();
                    Result result = this.curJobRunner.run(jobMeta.getJob());

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
                    LOGGER.info("Job execute completed : {}, time:{} ms.", jobMeta.getJob(), time);
                } catch (Throwable t) {
                    StringWriter sw = new StringWriter();
                    t.printStackTrace(new PrintWriter(sw));
                    response.setAction(Action.EXECUTE_EXCEPTION);
                    response.setMsg(sw.toString());
                    long time = SystemClock.now() - startTime;
                    stat.addRunningTime(time);
                    LOGGER.info("Job execute error : {}, time: {}, {}", jobMeta.getJob(), time, t.getMessage(), t);
                } finally {
                    checkInterrupted();
                    logger.removeId();
                    appContext.getRunnerPool().getRunningJobManager()
                            .out(jobMeta.getJobId());
                }
                // 统计数据
                stat(response.getAction());

                if (isStopToGetNewJob()) {
                    response.setReceiveNewJob(false);
                }
                this.jobMeta = callback.runComplete(response);

            }
        } finally {
            DtaLoggerFactory.remove();

            blockedOn(null);
        }
    }

    private void interrupt() {
        if (!interrupted.compareAndSet(false, true)) {
            return;
        }
        if (this.curJobRunner != null && this.curJobRunner instanceof InterruptibleJobRunner) {
            ((InterruptibleJobRunner) this.curJobRunner).interrupt();
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

    public JobMeta currentJob() {
        return jobMeta;
    }
}
