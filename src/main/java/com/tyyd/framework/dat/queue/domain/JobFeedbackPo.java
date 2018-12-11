package com.tyyd.framework.dat.queue.domain;


import com.tyyd.framework.dat.core.domain.TaskRunResult;

/**
 * @author Robert HG (254963746@qq.com) on 3/3/15.
 */
public class JobFeedbackPo{

    private String id;

    private Long gmtCreated;

    private TaskRunResult jobRunResult;

    public TaskRunResult getJobRunResult() {
        return jobRunResult;
    }

    public void setJobRunResult(TaskRunResult jobRunResult) {
        this.jobRunResult = jobRunResult;
    }

    public Long getGmtCreated() {
        return gmtCreated;
    }

    public void setGmtCreated(Long gmtCreated) {
        this.gmtCreated = gmtCreated;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
