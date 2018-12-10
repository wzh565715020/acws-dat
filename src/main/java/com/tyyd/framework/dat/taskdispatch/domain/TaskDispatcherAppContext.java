package com.tyyd.framework.dat.taskdispatch.domain;

import com.tyyd.framework.dat.biz.logger.JobLogger;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.queue.CronJobQueue;
import com.tyyd.framework.dat.queue.ExecutableJobQueue;
import com.tyyd.framework.dat.queue.ExecutingJobQueue;
import com.tyyd.framework.dat.queue.JobFeedbackQueue;
import com.tyyd.framework.dat.queue.NodeGroupStore;
import com.tyyd.framework.dat.queue.PreLoader;
import com.tyyd.framework.dat.queue.RepeatJobQueue;
import com.tyyd.framework.dat.queue.SuspendJobQueue;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelManager;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;
import com.tyyd.framework.dat.taskdispatch.support.TaskReceiver;
import com.tyyd.framework.dat.taskdispatch.support.OldDataHandler;
import com.tyyd.framework.dat.taskdispatch.support.TaskPushMachine;
import com.tyyd.framework.dat.taskdispatch.support.checker.ExecutingDeadTaskChecker;
import com.tyyd.framework.dat.taskdispatch.support.cluster.TaskClientManager;
import com.tyyd.framework.dat.taskdispatch.support.cluster.TaskExecuterManager;
import com.tyyd.framework.dat.taskexecuter.support.TaskPullMachine;

/**
 * JobTracker Application
 */
public class TaskDispatcherAppContext extends AppContext {

    private RemotingClientDelegate remotingClientDelegate;
    // channel manager
    private ChannelManager channelManager;
    // JobClient manager for job tracker
    private TaskClientManager taskClientManager;
    // TaskTracker manager for job tracker
    private TaskExecuterManager taskExecuterManager;
    // dead job checker
    private ExecutingDeadTaskChecker executingDeadJobChecker;
    // old data handler, dirty data
    private OldDataHandler oldDataHandler;
    // biz logger
    private JobLogger jobLogger;

    // executable job queue（waiting for exec）
    private ExecutableJobQueue executableJobQueue;
    // executing job queue
    private ExecutingJobQueue executingJobQueue;
    // store the connected node groups
    private NodeGroupStore nodeGroupStore;

    // Cron Job queue
    private CronJobQueue cronJobQueue;
    // feedback queue
    private JobFeedbackQueue jobFeedbackQueue;
    // job id generator
    private IdGenerator idGenerator;
	private SuspendJobQueue suspendJobQueue;
    private RepeatJobQueue repeatJobQueue;
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

    public JobFeedbackQueue getJobFeedbackQueue() {
        return jobFeedbackQueue;
    }

    public void setJobFeedbackQueue(JobFeedbackQueue jobFeedbackQueue) {
        this.jobFeedbackQueue = jobFeedbackQueue;
    }

    public RemotingClientDelegate getRemotingServer() {
        return remotingClientDelegate;
    }

    public void setRemotingServer(RemotingClientDelegate remotingClientDelegate) {
        this.remotingClientDelegate = remotingClientDelegate;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public void setChannelManager(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    public TaskClientManager getJobClientManager() {
        return taskClientManager;
    }

    public void setTaskClientManager(TaskClientManager jobClientManager) {
        this.taskClientManager = jobClientManager;
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

    public CronJobQueue getCronJobQueue() {
        return cronJobQueue;
    }

    public void setCronJobQueue(CronJobQueue cronJobQueue) {
        this.cronJobQueue = cronJobQueue;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public ExecutableJobQueue getExecutableJobQueue() {
        return executableJobQueue;
    }

    public void setExecutableJobQueue(ExecutableJobQueue executableJobQueue) {
        this.executableJobQueue = executableJobQueue;
    }

    public ExecutingJobQueue getExecutingJobQueue() {
        return executingJobQueue;
    }

    public void setExecutingJobQueue(ExecutingJobQueue executingJobQueue) {
        this.executingJobQueue = executingJobQueue;
    }

    public NodeGroupStore getNodeGroupStore() {
        return nodeGroupStore;
    }

    public void setNodeGroupStore(NodeGroupStore nodeGroupStore) {
        this.nodeGroupStore = nodeGroupStore;
    }

	public SuspendJobQueue getSuspendJobQueue() {
		return suspendJobQueue;
	}

	public void setSuspendJobQueue(SuspendJobQueue suspendJobQueue) {
		this.suspendJobQueue = suspendJobQueue;
	}

    public RepeatJobQueue getRepeatJobQueue() {
        return repeatJobQueue;
    }

    public void setRepeatJobQueue(RepeatJobQueue repeatJobQueue) {
        this.repeatJobQueue = repeatJobQueue;
    }

	public void setTaskPushMachine(TaskPushMachine taskPushMachine) {
		this.taskPushMachine = taskPushMachine;
	}

	public TaskPushMachine getTaskPushMachine() {
		return taskPushMachine;
	}
}
