package com.tyyd.framework.dat.management.cluster;

import com.tyyd.framework.dat.biz.logger.JobLogger;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.management.access.face.BackendJVMGCAccess;
import com.tyyd.framework.dat.management.access.face.BackendJVMMemoryAccess;
import com.tyyd.framework.dat.management.access.face.BackendJVMThreadAccess;
import com.tyyd.framework.dat.management.access.face.BackendJobClientMAccess;
import com.tyyd.framework.dat.management.access.face.BackendJobTrackerMAccess;
import com.tyyd.framework.dat.management.access.face.BackendNodeOnOfflineLogAccess;
import com.tyyd.framework.dat.management.access.face.BackendTaskTrackerMAccess;
import com.tyyd.framework.dat.management.access.memory.NodeMemCacheAccess;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.TaskFeedbackQueue;
import com.tyyd.framework.dat.queue.TaskQueue;
import com.tyyd.framework.dat.queue.SuspendTaskQueue;

public class BackendAppContext extends AppContext {

    private TaskQueue taskQueue;
    private ExecutableTaskQueue executableJobQueue;
    private ExecutingTaskQueue executingJobQueue;
    private TaskFeedbackQueue jobFeedbackQueue;
	private SuspendTaskQueue suspendJobQueue;
    private JobLogger jobLogger;
    private Node node;

    private BackendJobClientMAccess backendJobClientMAccess;
    private BackendJobTrackerMAccess backendJobTrackerMAccess;
    private BackendTaskTrackerMAccess backendTaskTrackerMAccess;
    private BackendJVMGCAccess backendJVMGCAccess;
    private BackendJVMMemoryAccess backendJVMMemoryAccess;
    private BackendJVMThreadAccess backendJVMThreadAccess;
    private BackendNodeOnOfflineLogAccess backendNodeOnOfflineLogAccess;

    private NodeMemCacheAccess nodeMemCacheAccess;

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

	public SuspendTaskQueue getSuspendJobQueue() {
		return suspendJobQueue;
	}

	public void setSuspendJobQueue(SuspendTaskQueue suspendJobQueue) {
		this.suspendJobQueue = suspendJobQueue;
	}

    public TaskFeedbackQueue getJobFeedbackQueue() {
        return jobFeedbackQueue;
    }

    public void setJobFeedbackQueue(TaskFeedbackQueue jobFeedbackQueue) {
        this.jobFeedbackQueue = jobFeedbackQueue;
    }

    public JobLogger getJobLogger() {
        return jobLogger;
    }

    public void setJobLogger(JobLogger jobLogger) {
        this.jobLogger = jobLogger;
    }

    public BackendJobClientMAccess getBackendJobClientMAccess() {
        return backendJobClientMAccess;
    }

    public void setBackendJobClientMAccess(BackendJobClientMAccess backendJobClientMAccess) {
        this.backendJobClientMAccess = backendJobClientMAccess;
    }

    public BackendJobTrackerMAccess getBackendJobTrackerMAccess() {
        return backendJobTrackerMAccess;
    }

    public void setBackendJobTrackerMAccess(BackendJobTrackerMAccess backendJobTrackerMAccess) {
        this.backendJobTrackerMAccess = backendJobTrackerMAccess;
    }

    public BackendTaskTrackerMAccess getBackendTaskTrackerMAccess() {
        return backendTaskTrackerMAccess;
    }

    public void setBackendTaskTrackerMAccess(BackendTaskTrackerMAccess backendTaskTrackerMAccess) {
        this.backendTaskTrackerMAccess = backendTaskTrackerMAccess;
    }

    public BackendJVMGCAccess getBackendJVMGCAccess() {
        return backendJVMGCAccess;
    }

    public void setBackendJVMGCAccess(BackendJVMGCAccess backendJVMGCAccess) {
        this.backendJVMGCAccess = backendJVMGCAccess;
    }

    public BackendJVMMemoryAccess getBackendJVMMemoryAccess() {
        return backendJVMMemoryAccess;
    }

    public void setBackendJVMMemoryAccess(BackendJVMMemoryAccess backendJVMMemoryAccess) {
        this.backendJVMMemoryAccess = backendJVMMemoryAccess;
    }

    public BackendJVMThreadAccess getBackendJVMThreadAccess() {
        return backendJVMThreadAccess;
    }

    public void setBackendJVMThreadAccess(BackendJVMThreadAccess backendJVMThreadAccess) {
        this.backendJVMThreadAccess = backendJVMThreadAccess;
    }

    public BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess() {
        return backendNodeOnOfflineLogAccess;
    }

    public void setBackendNodeOnOfflineLogAccess(BackendNodeOnOfflineLogAccess backendNodeOnOfflineLogAccess) {
        this.backendNodeOnOfflineLogAccess = backendNodeOnOfflineLogAccess;
    }

    public NodeMemCacheAccess getNodeMemCacheAccess() {
        return nodeMemCacheAccess;
    }

    public void setNodeMemCacheAccess(NodeMemCacheAccess nodeMemCacheAccess) {
        this.nodeMemCacheAccess = nodeMemCacheAccess;
    }
}
