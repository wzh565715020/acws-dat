package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.remoting.annotation.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskTracker task completed request command body
 */
public class TaskCompletedRequest extends AbstractRemotingCommandBody {
	private static final long serialVersionUID = 3034213298501228160L;

    @NotNull
    private List<TaskRunResult> taskRunResults;

    // 是否是重发(重发是批量发)
    private boolean reSend = false;

    public boolean isReSend() {
        return reSend;
    }

    public void setReSend(boolean reSend) {
        this.reSend = reSend;
    }

    public List<TaskRunResult> getTaskRunResults() {
        return taskRunResults;
    }

    public void setTaskRunResults(List<TaskRunResult> taskRunResults) {
        this.taskRunResults = taskRunResults;
    }

    public void addTaskResult(TaskRunResult taskRunResult) {
        if (taskRunResults == null) {
            taskRunResults = new ArrayList<TaskRunResult>();
        }
        taskRunResults.add(taskRunResult);
    }
}
