package com.tyyd.framework.dat.taskdispatch.domain;

import java.util.List;

import com.tyyd.framework.dat.biz.logger.TaskLogger;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutedTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.PoolQueue;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;
import com.tyyd.framework.dat.taskdispatch.support.TaskReceiver;
import com.tyyd.framework.dat.taskdispatch.support.TaskPushMachine;
import com.tyyd.framework.dat.taskdispatch.support.checker.ExecutingDeadTaskChecker;
import com.tyyd.framework.dat.taskdispatch.support.cluster.TaskDispatcherManager;
import com.tyyd.framework.dat.taskdispatch.support.cluster.TaskExecuterManager;

/**
 * JobTracker Application
 */
public class TaskDispatcherAppContext extends AppContext {

    private RemotingClientDelegate remotingClient;
    
    private RemotingServerDelegate remotingServer;
    // channel manager
   
    // dead task checker
    private ExecutingDeadTaskChecker executingDeadJobChecker;
    // biz logger
    private TaskLogger taskLogger;
    
    // executable task queue（waiting for exec）
    private ExecutableTaskQueue executableTaskQueue;
    // executing task queue
    private ExecutingTaskQueue executingTaskQueue;
    // executing task queue
    private ExecutedTaskQueue executedTaskQueue;
    // task queue
    private PoolQueue poolQueue;
    // task id generator
    private IdGenerator idGenerator;
    private TaskReceiver taskReceiver;
    private TaskSender taskSender;

    // Pull task Machine
    private TaskPushMachine taskPushMachine;
    
    private List<PoolPo> poolPoList;
    
    private TaskDispatcherManager taskDispatcherManager;
    
    private TaskExecuterManager taskExecuterManager;
    
    public TaskExecuterManager getTaskExecuterManager() {
		return taskExecuterManager;
	}

	public void setTaskExecuterManager(TaskExecuterManager taskExecuterManager) {
		this.taskExecuterManager = taskExecuterManager;
	}

	public TaskDispatcherManager getTaskDispatcherManager() {
		return taskDispatcherManager;
	}

	public void setTaskDispatcherManager(TaskDispatcherManager taskDispatcherManager) {
		this.taskDispatcherManager = taskDispatcherManager;
	}

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

    public TaskLogger getTaskLogger() {
        return taskLogger;
    }

    public void setTaskLogger(TaskLogger taskLogger) {
        this.taskLogger = taskLogger;
    }


    public RemotingClientDelegate getRemotingClient() {
        return remotingClient;
    }

    public void setRemotingClient(RemotingClientDelegate remotingClientDelegate) {
        this.remotingClient = remotingClientDelegate;
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

    public void setExecutableTaskQueue(ExecutableTaskQueue executableTaskQueue) {
        this.executableTaskQueue = executableTaskQueue;
    }

    public ExecutingTaskQueue getExecutingTaskQueue() {
        return executingTaskQueue;
    }

    public void setExecutingTaskQueue(ExecutingTaskQueue executingTaskQueue) {
        this.executingTaskQueue = executingTaskQueue;
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

	public RemotingServerDelegate getRemotingServer() {
		return remotingServer;
	}

	public void setRemotingServer(RemotingServerDelegate remotingServer) {
		this.remotingServer = remotingServer;
	}

	public ExecutedTaskQueue getExecutedTaskQueue() {
		return executedTaskQueue;
	}

	public void setExecutedTaskQueue(ExecutedTaskQueue executedTaskQueue) {
		this.executedTaskQueue = executedTaskQueue;
	}


}
