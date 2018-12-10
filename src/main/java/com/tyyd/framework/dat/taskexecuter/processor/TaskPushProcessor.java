package com.tyyd.framework.dat.taskexecuter.processor;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.tyyd.framework.dat.core.commons.utils.Callable;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.JobMeta;
import com.tyyd.framework.dat.core.domain.JobRunResult;
import com.tyyd.framework.dat.core.exception.JobTrackerNotFoundException;
import com.tyyd.framework.dat.core.exception.RequestTimeoutException;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.JobCompletedRequest;
import com.tyyd.framework.dat.core.protocol.command.JobPushRequest;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.support.NodeShutdownHook;
import com.tyyd.framework.dat.core.support.RetryScheduler;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.ResponseFuture;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskexecuter.domain.Response;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;
import com.tyyd.framework.dat.taskexecuter.expcetion.NoAvailableTaskRunnerException;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerCallback;

/**
 *         接受任务并执行
 */
public class TaskPushProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskPushProcessor.class);

    private RetryScheduler<JobRunResult> retryScheduler;
    private JobRunnerCallback jobRunnerCallback;
    private RemotingClientDelegate remotingClient;

    protected TaskPushProcessor(TaskExecuterAppContext appContext) {
        super(appContext);
        this.remotingClient = appContext.getRemotingClient();
        retryScheduler = new RetryScheduler<JobRunResult>(appContext, 3) {
            @Override
            protected boolean isRemotingEnable() {
                return remotingClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<JobRunResult> results) {
                return retrySendJobResults(results);
            }
        };
        retryScheduler.setName("JobPush");
        retryScheduler.start();

        // 线程安全的
        jobRunnerCallback = new JobRunnerCallback();

        NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new Callable() {
            @Override
            public void call() throws Exception {
                retryScheduler.stop();
            }
        });
    }

    @Override
    public RemotingCommand processRequest(Channel channel,
                                          final RemotingCommand request) throws RemotingCommandException {

        JobPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final JobMeta jobMeta = requestBody.getJobMeta();

        try {
            appContext.getRunnerPool().execute(jobMeta, jobRunnerCallback);
        } catch (NoAvailableTaskRunnerException e) {
            // 任务推送失败
            return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.NO_AVAILABLE_JOB_RUNNER.code(),
                    "job push failure , no available job runner!");
        }

        // 任务推送成功
        return RemotingCommand.createResponseCommand(JobProtos
                .ResponseCode.TASK_PUSH_SUCCESS.code(), "job push success!");
    }

    /**
     * 任务执行的回调(任务执行完之后线程回调这个函数)
     */
    private class JobRunnerCallback implements RunnerCallback {
        @Override
        public JobMeta runComplete(Response response) {
            // 发送消息给 JobTracker
            final JobRunResult jobRunResult = new JobRunResult();
            jobRunResult.setTime(SystemClock.now());
            jobRunResult.setJobMeta(response.getJobMeta());
            jobRunResult.setAction(response.getAction());
            jobRunResult.setMsg(response.getMsg());
            JobCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
            requestBody.addJobResult(jobRunResult);
            requestBody.setReceiveNewJob(response.isReceiveNewJob());     // 设置可以接受新任务

            int requestCode = JobProtos.RequestCode.TASK_COMPLETED.code();

            RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

            final Response returnResponse = new Response();

            try {
                final CountDownLatch latch = new CountDownLatch(1);
                remotingClient.invokeAsync(request, new AsyncCallback() {
                    @Override
                    public void operationComplete(ResponseFuture responseFuture) {
                        try {
                            RemotingCommand commandResponse = responseFuture.getResponseCommand();

                            if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                                JobPushRequest jobPushRequest = commandResponse.getBody();
                                if (jobPushRequest != null) {
                                    LOGGER.info("Get new job :{}", jobPushRequest.getJobMeta());
                                    returnResponse.setJobMeta(jobPushRequest.getJobMeta());
                                }
                            } else {
                                LOGGER.info("Job feedback failed, save local files。{}", jobRunResult);
                                try {
                                    retryScheduler.inSchedule(
                                            jobRunResult.getJobMeta().getJobId().concat("_") + SystemClock.now(),
                                            jobRunResult);
                                } catch (Exception e) {
                                    LOGGER.error("Job feedback failed", e);
                                }
                            }
                        } finally {
                            latch.countDown();
                        }
                    }
                });

                try {
                    latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RequestTimeoutException(e);
                }
            } catch (JobTrackerNotFoundException e) {
                try {
                    LOGGER.warn("No job tracker available! save local files.");
                    retryScheduler.inSchedule(
                            jobRunResult.getJobMeta().getJobId().concat("_") + SystemClock.now(),
                            jobRunResult);
                } catch (Exception e1) {
                    LOGGER.error("Save files failed, {}", jobRunResult.getJobMeta(), e1);
                }
            }

            return returnResponse.getJobMeta();
        }
    }

    /**
     * 发送JobResults
     */
    private boolean retrySendJobResults(List<JobRunResult> results) {
        // 发送消息给 JobTracker
        JobCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
        requestBody.setJobRunResults(results);
        requestBody.setReSend(true);

        int requestCode = JobProtos.RequestCode.TASK_COMPLETED.code();
        RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

        try {
            // 这里一定要用同步，不然异步会发生文件锁，死锁
            RemotingCommand commandResponse = remotingClient.invokeSync(request);
            if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                return true;
            } else {
                LOGGER.warn("Send job failed, {}", commandResponse);
                return false;
            }
        } catch (JobTrackerNotFoundException e) {
            LOGGER.error("Retry send job result failed! jobResults={}", results, e);
        }
        return false;
    }

}
