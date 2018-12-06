package com.tyyd.framework.dat.taskclient;


import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.core.support.RetryScheduler;
import com.tyyd.framework.dat.taskclient.domain.TaskClientAppContext;
import com.tyyd.framework.dat.taskclient.domain.TaskClientNode;
import com.tyyd.framework.dat.taskclient.domain.Response;
import com.tyyd.framework.dat.taskclient.domain.ResponseCode;
import com.tyyd.framework.dat.taskclient.support.TaskSubmitProtectException;


/**
 *         重试 客户端, 如果 没有可用的JobTracker, 那么存文件, 定时重试
 */
public class RetryJobClient extends TaskClient<TaskClientNode, TaskClientAppContext> {

    private RetryScheduler<Job> retryScheduler;

    @Override
    protected void beforeStart() {
        super.beforeStart();
        retryScheduler = new RetryScheduler<Job>(appContext, 30) {
            @Override
            protected boolean isRemotingEnable() {
                return isServerEnable();
            }

            @Override
            protected boolean retry(List<Job> jobs) {
                Response response = null;
                try {
                    // 重试必须走同步，不然会造成文件锁，死锁
                    response = superSubmitJob(jobs, SubmitType.SYNC);
                    return response.isSuccess();
                } catch (Throwable t) {
                    RetryScheduler.LOGGER.error(t.getMessage(), t);
                } finally {
                    if (response != null && response.isSuccess()) {
                        stat.incSubmitFailStoreNum(jobs.size());
                    }
                }
                return false;
            }
        };
        retryScheduler.setName(RetryJobClient.class.getSimpleName());
        retryScheduler.start();
    }

    @Override
    protected void beforeStop() {
        super.beforeStop();
        retryScheduler.stop();
    }

    @Override
    public Response submitJob(Job job) {
        return submitJob(Collections.singletonList(job));
    }

    @Override
    public Response submitJob(List<Job> jobs) {

        Response response;
        try {
            response = superSubmitJob(jobs);
        } catch (TaskSubmitProtectException e) {
            response = new Response();
            response.setSuccess(false);
            response.setFailedJobs(jobs);
            response.setCode(ResponseCode.SUBMIT_TOO_BUSY_AND_SAVE_FOR_LATER);
            response.setMsg(response.getMsg() + ", submit too busy , save local fail store and send later !");
        }
        if (!response.isSuccess()) {
            try {
                for (Job job : response.getFailedJobs()) {
                    retryScheduler.inSchedule(job.getTaskId(), job);
                    stat.incFailStoreNum();
                }
                response.setSuccess(true);
                response.setCode(ResponseCode.SUBMIT_FAILED_AND_SAVE_FOR_LATER);
                response.setMsg(response.getMsg() + ", save local fail store and send later !");
                LOGGER.warn(JSON.toJSONString(response));
            } catch (Exception e) {
                response.setSuccess(false);
                response.setMsg(e.getMessage());
            }
        }

        return response;
    }

    private Response superSubmitJob(List<Job> jobs) {
        return super.submitJob(jobs);
    }

    private Response superSubmitJob(List<Job> jobs, SubmitType type) {
        return super.submitJob(jobs, type);
    }
}
