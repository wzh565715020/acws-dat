package com.tyyd.framework.dat.taskclient.support;


import java.util.List;
import java.util.concurrent.TimeUnit;

import com.tyyd.framework.dat.core.commons.concurrent.limiter.RateLimiter;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.core.exception.JobSubmitException;
import com.tyyd.framework.dat.taskclient.domain.TaskClientAppContext;
import com.tyyd.framework.dat.taskclient.domain.Response;

/**
 * 用来处理客户端请求过载问题
 *
 */
public class TaskSubmitProtector {

    private int maxQPS;
    // 用信号量进行过载保护
    RateLimiter rateLimiter;
    private int acquireTimeout = 100;
    private String errorMsg;

    public TaskSubmitProtector(TaskClientAppContext appContext) {

        this.maxQPS = appContext.getConfig().getParameter(Constants.TASK_SUBMIT_MAX_QPS,
                Constants.DEFAULT_TASK_SUBMIT_MAX_QPS);
        if (this.maxQPS < 10) {
            this.maxQPS = Constants.DEFAULT_TASK_SUBMIT_MAX_QPS;
        }

        this.errorMsg = "the maxQPS is " + maxQPS +
                " , submit too fast , use " + Constants.TASK_SUBMIT_MAX_QPS +
                " can change the concurrent size .";
        this.acquireTimeout = appContext.getConfig().getParameter("job.submit.lock.acquire.timeout", 100);

        this.rateLimiter = RateLimiter.create(this.maxQPS);
    }

    public Response execute(final List<Job> jobs, final TaskSubmitExecutor<Response> jobSubmitExecutor) throws JobSubmitException {
        if (!rateLimiter.tryAcquire(acquireTimeout, TimeUnit.MILLISECONDS)) {
            throw new TaskSubmitProtectException(maxQPS, errorMsg);
        }
        return jobSubmitExecutor.execute(jobs);
    }

    public int getMaxQPS() {
        return maxQPS;
    }
}
