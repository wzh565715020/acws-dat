package com.tyyd.framework.dat.core.protocol.command;


import com.tyyd.framework.dat.core.domain.Task;

import java.util.List;

/**
 * 任务传递信息
 */
public class TaskSubmitResponse extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 9133108871954698698L;

	private Boolean success = true;

    private String msg;

    // 失败的jobs
    private List<Task> failedJobs;

    public List<Task> getFailedJobs() {
        return failedJobs;
    }

    public void setFailedJobs(List<Task> failedJobs) {
        this.failedJobs = failedJobs;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
