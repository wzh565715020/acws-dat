package com.tyyd.framework.dat.taskdispatch.domain;

import com.tyyd.framework.dat.biz.logger.JobLogger;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.TaskFeedbackQueue;
import com.tyyd.framework.dat.queue.PreLoader;
import com.tyyd.framework.dat.queue.TaskQueue;
import com.tyyd.framework.dat.queue.SuspendTaskQueue;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelManager;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;
import com.tyyd.framework.dat.taskdispatch.support.TaskReceiver;
import com.tyyd.framework.dat.taskdispatch.support.OldDataHandler;
import com.tyyd.framework.dat.taskdispatch.support.TaskPushMachine;
import com.tyyd.framework.dat.taskdispatch.support.checker.ExecutingDeadTaskChecker;
import com.tyyd.framework.dat.taskdispatch.support.cluster.TaskExecuterManager;

/**
 * JobTracker Application
 */
public class TaskDispatcherAppContext extends AppContext {

    private RemotingClientDelegate remotingClient;
    // channel manager
    private ChannelManager channelManager;
    // TaskTracker manager for job tracker
    private TaskExecuterManager taskExecuterManager;
    // dead job checker
    private ExecutingDeadTaskChecker executingDeadJobChecker;
    // old data handler, dirty data
    private OldDataHandler oldDataHandler;
    // biz logger
    private JobLogger jobLogger;
    
    // executable job queue（waiting for exec）
    private ExecutableTaskQueue executableTaskQueue;
    // executing job queue
    private ExecutingTaskQueue executingTaskQueue;

    // Cron Job queue
    private TaskQueue taskQueue;
    // feedback queue
    private TaskFeedbackQueue jobFeedbackQueue;
    // job id generator
    private IdGenerator idGenerator;
	private SuspendTaskQueue suspendJobQueue;
    private PreLoader preLoader;
    private TaskReceiver jobReceiver;
    private TaskSender jobSender;

    // Pull Job Machine
    private TaskPushMachine taskPushMachine;
    public TaskSender getJobSender() {
        return jobSender;
    }

    public void setJobSender(TaskSender jobSender) {
        this.jobSender = jobSender;
    }

    public TaskReceiver getJobReceiver() {
        return jobReceiver;
    }

    public void setJobReceiver(TaskReceiver jobReceiver) {
        this.jobReceiver = jobReceiver;
    }

    public PreLoader getPreLoader() {
        return preLoader;
    }

    public void setPreLoader(PreLoader preLoader) {
        this.preLoader = preLoader;
    }

    public JobLogger getJobLogger() {
        return jobLogger;
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
    }

    public TaskFeedbackQueue getJobFeedbackQueue() {
        return jobFeedbackQueue;
    }

    public void setJobFeedbackQueue(TaskFeedbackQueue jobFeedbackQueue) {
        this.jobFeedbackQueue = jobFeedbackQueue;
    }

    public RemotingClientDelegate getRemotingServer() {
        return remotingClient;
    }

    public void setRemotingServer(RemotingClientDelegate remotingClientDelegate) {
        this.remotingClient = remotingClientDelegate;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public void setChannelManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public TaskExecuterManager getTaskExecuterManager() {
        return taskExecuterManager;
    }

    public void setTaskExecuterManager(TaskExecuterManager taskExecuterManager) {
        this.taskExecuterManager = taskExecuterManager;
    }

    public ExecutingDeadTaskChecker getExecutingDeadJobChecker() {
        return executingDeadJobChecker;
    }

    public void setExecutingDeadJobChecker(ExecutingDeadTaskChecker executingDeadJobChecker) {
        this.executingDeadJobChecker = executingDeadJobChecker;
    }

    public OldDataHandler getOldDataHandler() {
        return oldDataHandler;
    }

    public void setOldDataHandler(OldDataHandler oldDataHandler) {
        this.oldDataHandler = oldDataHandler;
    }


    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public ExecutableTaskQueue getExecutableJobQueue() {
        return executableTaskQueue;
    }

    public void setExecutableJobQueue(ExecutableTaskQueue executableJobQueue) {
        this.executableTaskQueue = executableJobQueue;
    }

    public ExecutingTaskQueue getExecutingJobQueue() {
        return executingTaskQueue;
    }

    public void setExecutingJobQueue(ExecutingTaskQueue executingJobQueue) {
        this.executingTaskQueue = executingJobQueue;
    }

	public SuspendTaskQueue getSuspendJobQueue() {
		return suspendJobQueue;
	}

	public void setSuspendJobQueue(SuspendTaskQueue suspendJobQueue) {
		this.suspendJobQueue = suspendJobQueue;
	}

	public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public void setTaskPushMachine(TaskPushMachine taskPushMachine) {
		this.taskPushMachine = taskPushMachine;
	}

	public TaskPushMachine getTaskPushMachine() {
		return taskPushMachine;
	}


}
