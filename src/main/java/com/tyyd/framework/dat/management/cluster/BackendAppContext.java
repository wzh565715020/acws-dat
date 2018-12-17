package com.tyyd.framework.dat.management.cluster;

import com.tyyd.framework.dat.biz.logger.TaskLogger;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.TaskQueue;

public class BackendAppContext extends AppContext {

    private TaskQueue taskQueue;
    private ExecutableTaskQueue executableJobQueue;
    private ExecutingTaskQueue executingJobQueue;
    private TaskLogger jobLogger;
    private Node node;

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public TaskQueue getTaskQueue() {
		return taskQueue;
	}

	public void setTaskQueue(TaskQueue taskQueue) {
		this.taskQueue = taskQueue;
	}

	public ExecutableTaskQueue getExecutableJobQueue() {
        return executableJobQueue;
    }

    public void setExecutableJobQueue(ExecutableTaskQueue executableJobQueue) {
        this.executableJobQueue = executableJobQueue;
    }

    public ExecutingTaskQueue getExecutingJobQueue() {
        return executingJobQueue;
    }

    public void setExecutingJobQueue(ExecutingTaskQueue executingJobQueue) {
        this.executingJobQueue = executingJobQueue;
    }

    public TaskLogger getJobLogger() {
        return jobLogger;
    }

    public void setJobLogger(TaskLogger jobLogger) {
        this.jobLogger = jobLogger;
    }

}
