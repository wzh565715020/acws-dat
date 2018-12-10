package com.tyyd.framework.dat.taskclient;


import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.AbstractClientNode;
import com.tyyd.framework.dat.core.commons.utils.Assert;
import com.tyyd.framework.dat.core.commons.utils.BatchUtils;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.core.exception.JobSubmitException;
import com.tyyd.framework.dat.core.exception.JobTrackerNotFoundException;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.CommandBodyWrapper;
import com.tyyd.framework.dat.core.protocol.command.JobCancelRequest;
import com.tyyd.framework.dat.core.protocol.command.JobSubmitRequest;
import com.tyyd.framework.dat.core.protocol.command.JobSubmitResponse;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.ResponseFuture;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskclient.domain.TaskClientAppContext;
import com.tyyd.framework.dat.taskclient.domain.TaskClientNode;
import com.tyyd.framework.dat.taskclient.domain.Response;
import com.tyyd.framework.dat.taskclient.domain.ResponseCode;
import com.tyyd.framework.dat.taskclient.processor.RemotingDispatcher;
import com.tyyd.framework.dat.taskclient.support.TaskClientMStatReporter;
import com.tyyd.framework.dat.taskclient.support.TaskCompletedHandler;
import com.tyyd.framework.dat.taskclient.support.TaskSubmitExecutor;
import com.tyyd.framework.dat.taskclient.support.TaskSubmitProtector;
import com.tyyd.framework.dat.taskclient.support.SubmitCallback;

/**
 *         任务客户端
 */
public class TaskClient<T extends TaskClientNode, Context extends AppContext> extends
        AbstractClientNode<TaskClientNode, TaskClientAppContext> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TaskClient.class);

    private static final int BATCH_SIZE = 50;

    // 过载保护的提交者
    private TaskSubmitProtector protector;
    protected TaskClientMStatReporter stat;

    public TaskClient() {
        this.stat = new TaskClientMStatReporter(appContext);
        // 监控中心
        appContext.setMStatReporter(stat);
    }

    @Override
    protected void beforeStart() {
        appContext.setRemotingClient(remotingClient);
        protector = new TaskSubmitProtector(appContext);
    }

    @Override
    protected void afterStart() {
        appContext.getMStatReporter().start();
    }

    @Override
    protected void afterStop() {
        appContext.getMStatReporter().stop();
    }

    @Override
    protected void beforeStop() {
    }

    public Response submitJob(Job job) throws JobSubmitException {
        checkStart();
        return protectSubmit(Collections.singletonList(job));
    }

    private Response protectSubmit(List<Job> jobs) throws JobSubmitException {
        return protector.execute(jobs, new TaskSubmitExecutor<Response>() {
            @Override
            public Response execute(List<Job> jobs) throws JobSubmitException {
                return submitJob(jobs, SubmitType.ASYNC);
            }
        });
    }

    /**
     * 取消任务
     */
    public Response cancelJob(String taskId, String taskTrackerNodeGroup) {
        checkStart();

        final Response response = new Response();

        Assert.hasText(taskId, "taskId can not be empty");
        Assert.hasText(taskTrackerNodeGroup, "taskTrackerNodeGroup can not be empty");

        JobCancelRequest request = CommandBodyWrapper.wrapper(appContext, new JobCancelRequest());
        request.setTaskId(taskId);
        request.setTaskTrackerNodeGroup(taskTrackerNodeGroup);

        RemotingCommand requestCommand = RemotingCommand.createRequestCommand(
                JobProtos.RequestCode.CANCEL_TASK.code(), request);

        try {
            RemotingCommand remotingResponse = remotingClient.invokeSync(requestCommand);

            if (JobProtos.ResponseCode.TASK_CANCEL_SUCCESS.code() == remotingResponse.getCode()) {
                LOGGER.info("Cancel job success taskId={}, taskTrackerNodeGroup={} ", taskId, taskTrackerNodeGroup);
                response.setSuccess(true);
                return response;
            }

            response.setSuccess(false);
            response.setCode(JobProtos.ResponseCode.valueOf(remotingResponse.getCode()).name());
            response.setMsg(remotingResponse.getRemark());
            LOGGER.warn("Cancel job failed: taskId={}, taskTrackerNodeGroup={}, msg={}", taskId,
                    taskTrackerNodeGroup, remotingResponse.getRemark());
            return response;

        } catch (JobTrackerNotFoundException e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.JOB_TRACKER_NOT_FOUND);
            response.setMsg("Can not found JobTracker node!");
            return response;
        }
    }

    private void checkFields(List<Job> jobs) {
        // 参数验证
        if (CollectionUtils.isEmpty(jobs)) {
            throw new JobSubmitException("Job can not be null!");
        }
        for (Job job : jobs) {
            if (job == null) {
                throw new JobSubmitException("Job can not be null!");
            } else {
                job.checkField();
            }
        }
    }

    protected Response submitJob(final List<Job> jobs, SubmitType type) throws JobSubmitException {
        // 检查参数
        checkFields(jobs);

        final Response response = new Response();
        try {
            JobSubmitRequest jobSubmitRequest = CommandBodyWrapper.wrapper(appContext, new JobSubmitRequest());
            jobSubmitRequest.setJobs(jobs);

            RemotingCommand requestCommand = RemotingCommand.createRequestCommand(
                    JobProtos.RequestCode.SUBMIT_JOB.code(), jobSubmitRequest);

            SubmitCallback submitCallback = new SubmitCallback() {
                @Override
                public void call(RemotingCommand responseCommand) {
                    if (responseCommand == null) {
                        response.setFailedJobs(jobs);
                        response.setSuccess(false);
                        response.setMsg("Submit Job failed: JobTracker is broken");
                        LOGGER.warn("Submit Job failed: {}, {}", jobs, "JobTracker is broken");
                        return;
                    }

                    if (JobProtos.ResponseCode.TASK_RECEIVE_SUCCESS.code() == responseCommand.getCode()) {
                        LOGGER.info("Submit Job success: {}", jobs);
                        response.setSuccess(true);
                        return;
                    }
                    // 失败的job
                    JobSubmitResponse jobSubmitResponse = responseCommand.getBody();
                    response.setFailedJobs(jobSubmitResponse.getFailedJobs());
                    response.setSuccess(false);
                    response.setCode(JobProtos.ResponseCode.valueOf(responseCommand.getCode()).name());
                    response.setMsg("Submit Job failed: " + responseCommand.getRemark() + " " + jobSubmitResponse.getMsg());
                    LOGGER.warn("Submit Job failed: {}, {}, {}", jobs, responseCommand.getRemark(), jobSubmitResponse.getMsg());
                }
            };

            if (SubmitType.ASYNC.equals(type)) {
                asyncSubmit(requestCommand, submitCallback);
            } else {
                syncSubmit(requestCommand, submitCallback);
            }
        } catch (JobTrackerNotFoundException e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.JOB_TRACKER_NOT_FOUND);
            response.setMsg("Can not found JobTracker node!");
        } catch (Exception e) {
            response.setSuccess(false);
            response.setCode(ResponseCode.SYSTEM_ERROR);
            response.setMsg(StringUtils.toString(e));
        } finally {
            // 统计
            if (response.isSuccess()) {
                stat.incSubmitSuccessNum(jobs.size());
            } else {
                stat.incSubmitFailedNum(CollectionUtils.sizeOf(response.getFailedJobs()));
            }
        }

        return response;
    }

    /**
     * 异步提交任务
     */
    private void asyncSubmit(RemotingCommand requestCommand, final SubmitCallback submitCallback)
            throws JobTrackerNotFoundException {
        final CountDownLatch latch = new CountDownLatch(1);
        remotingClient.invokeAsync(requestCommand, new AsyncCallback() {
            @Override
            public void operationComplete(ResponseFuture responseFuture) {
                try {
                    submitCallback.call(responseFuture.getResponseCommand());
                } finally {
                    latch.countDown();
                }
            }
        });
        try {
            latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new JobSubmitException("Submit job failed, async request timeout!", e);
        }
    }

    /**
     * 同步提交任务
     */
    private void syncSubmit(RemotingCommand requestCommand, final SubmitCallback submitCallback)
            throws JobTrackerNotFoundException {
        submitCallback.call(remotingClient.invokeSync(requestCommand));
    }

    public Response submitJob(List<Job> jobs) throws JobSubmitException {
        checkStart();
        Response response = new Response();
        response.setSuccess(true);
        int size = jobs.size();
        for (int i = 0; i <= size / BATCH_SIZE; i++) {
            List<Job> subJobs = BatchUtils.getBatchList(i, BATCH_SIZE, jobs);

            if (CollectionUtils.isNotEmpty(subJobs)) {
                Response subResponse = protectSubmit(subJobs);
                if (!subResponse.isSuccess()) {
                    response.setSuccess(false);
                    response.addFailedJobs(subJobs);
                    response.setMsg(subResponse.getMsg());
                }
            }
        }

        return response;
    }

    @Override
    protected RemotingProcessor getDefaultProcessor() {
        return new RemotingDispatcher(appContext);
    }

    /**
     * 设置任务完成接收器
     */
    public void setJobFinishedHandler(TaskCompletedHandler jobCompletedHandler) {
        appContext.setJobCompletedHandler(jobCompletedHandler);
    }

    enum SubmitType {
        SYNC,   // 同步
        ASYNC   // 异步
    }

    private void checkStart(){
        if(!started.get()){
            throw new JobSubmitException("JobClient did not started");
        }
    }
}
