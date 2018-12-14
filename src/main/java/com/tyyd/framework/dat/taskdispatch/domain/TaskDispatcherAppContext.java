package com.tyyd.framework.dat.taskdispatch.domain;

import java.util.List;

import com.tyyd.framework.dat.biz.logger.TaskLogger;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.PoolQueue;
import com.tyyd.framework.dat.queue.PreLoader;
import com.tyyd.framework.dat.queue.TaskQueue;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelManager;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;
import com.tyyd.framework.dat.taskdispatch.support.TaskReceiver;
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
    // biz logger
    private TaskLogger taskLogger;
    
    // executable job queue（waiting for exec）
    private ExecutableTaskQueue executableTaskQueue;
    // executing job queue
    private ExecutingTaskQueue executingTaskQueue;

    // task queue
    private TaskQueue taskQueue;
    private PoolQueue poolQueue;
    // task id generator
    private IdGenerator idGenerator;
    private PreLoader preLoader;
    private TaskReceiver taskReceiver;
    private TaskSender taskSender;

    // Pull Job Machine
    private TaskPushMachine taskPushMachine;
    
    private List<PoolPo> poolPoList;
    
    public List<PoolPo> getPoolPoList() {
		return poolPoList;
	}

	public void setPoolPoList(List<PoolPo> poolPoList) {
		this.poolPoList = poolPoList;
	}

	public TaskSender getTaskSender() {
        return taskSender;
    }

    public void setTaskSender(TaskSender taskSender) {
        this.taskSender = taskSender;
    }

    public TaskReceiver getTaskReceiver() {
        return taskReceiver;
    }

    public void setTaskReceiver(TaskReceiver taskReceiver) {
        this.taskReceiver = taskReceiver;
    }

    public PreLoader getPreLoader() {
        return preLoader;
    }

    public void setPreLoader(PreLoader preLoader) {
        this.preLoader = preLoader;
    }

    public TaskLogger getTaskLogger() {
        return taskLogger;
    }

    public void setJobLogger(TaskLogger jobLogger) {
        this.taskLogger = jobLogger;
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

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public void setIdGenerator(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public ExecutableTaskQueue getExecutableTaskQueue() {
        return executableTaskQueue;
    }

    public void setExecutableJobQueue(ExecutableTaskQueue executableTaskQueue) {
        this.executableTaskQueue = executableTaskQueue;
    }

    public ExecutingTaskQueue getExecutingTaskQueue() {
        return executingTaskQueue;
    }

    public void setExecutingJobQueue(ExecutingTaskQueue executingTaskQueue) {
        this.executingTaskQueue = executingTaskQueue;
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

	public PoolQueue getPoolQueue() {
		return poolQueue;
	}

	public void setPoolQueue(PoolQueue poolQueue) {
		this.poolQueue = poolQueue;
	}


}
