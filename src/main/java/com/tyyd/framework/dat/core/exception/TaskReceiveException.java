package com.tyyd.framework.dat.core.exception;

import com.tyyd.framework.dat.core.domain.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端提交的任务 接受 异常
 */
public class TaskReceiveException extends Exception {

	private static final long serialVersionUID = 6091344409709022270L;
	/**
     * 出错的job列表
     */
    private List<Task> jobs;

    public List<Task> getJobs() {
        return jobs;
    }

    public void setJobs(List<Task> jobs) {
        this.jobs = jobs;
    }

    public void addJob(Task job){
        if(jobs == null){
            jobs = new ArrayList<Task>();
        }

        jobs.add(job);
    }

    public TaskReceiveException() {
    }

    public TaskReceiveException(String message) {
        super(message);
    }

    public TaskReceiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskReceiveException(Throwable cause) {
        super(cause);
    }

}
