package com.tyyd.framework.dat.taskexecuter.processor;


import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.core.exception.RequestTimeoutException;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskCompletedRequest;
import com.tyyd.framework.dat.core.protocol.command.TaskPushRequest;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.core.support.RetryScheduler;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingClient;
import com.tyyd.framework.dat.remoting.RemotingClientConfig;
import com.tyyd.framework.dat.remoting.RemotingTransporter;
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
public class TaskProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskProcessor.class);

    private TaskRunnerCallback taskRunnerCallback;
    private RemotingServerDelegate remotingServer;
    private RetryScheduler<TaskRunResult> retryScheduler;

    protected TaskProcessor(TaskExecuterAppContext appContext) {
        super(appContext);
        this.remotingServer = appContext.getRemotingServer();
        // 线程安全的
        taskRunnerCallback = new TaskRunnerCallback();
        retryScheduler = new RetryScheduler<TaskRunResult>(appContext, 3) {
            @Override
            protected boolean isRemotingEnable() {
                return remotingServer.isServerEnable();
            }

            @Override
            protected boolean retry(List<TaskRunResult> results) {
                return retrySendJobResults(results);
            }
        };
        retryScheduler.setName("TaskPushMassage");
        retryScheduler.start();
    }

    @Override
    public RemotingCommand processRequest(Channel channel,
                                          final RemotingCommand request) throws RemotingCommandException {

        TaskPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final TaskMeta taskMeta = requestBody.getTaskMeta();

        try {
            appContext.getRunnerPool().execute(channel,taskMeta, taskRunnerCallback);
        } catch (NoAvailableTaskRunnerException e) {
            // 任务推送失败
            return RemotingCommand.createResponseCommand(TaskProtos.ResponseCode.NO_AVAILABLE_JOB_RUNNER.code(),
                    "job push failure , no available task runner!");
        }

        // 任务推送成功
        return RemotingCommand.createResponseCommand(TaskProtos
                .ResponseCode.TASK_PUSH_SUCCESS.code(), "task push success!");
    }

    /**
     * 任务执行的回调(任务执行完之后线程回调这个函数)
     */
    private class TaskRunnerCallback implements RunnerCallback {
        @Override
        public void runComplete(Response response) {
            // 发送消息给 JobTracker
            final TaskRunResult taskRunResult = new TaskRunResult();
            taskRunResult.setTime(SystemClock.now());
            taskRunResult.setTaskMeta(response.getTaskMeta());
            taskRunResult.setAction(response.getAction());
            taskRunResult.setMsg(response.getMsg());
            TaskCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new TaskCompletedRequest());
            requestBody.addJobResult(taskRunResult);

            int requestCode = TaskProtos.RequestCode.TASK_COMPLETED.code();

            RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

            final Response returnResponse = new Response();

            try {
                final CountDownLatch latch = new CountDownLatch(1);
                remotingServer.invokeAsync(response.getChannel(),request, new AsyncCallback() {
                    @Override
                    public void operationComplete(ResponseFuture responseFuture) {
                        try {
                            RemotingCommand commandResponse = responseFuture.getResponseCommand();

                            if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                                TaskPushRequest jobPushRequest = commandResponse.getBody();
                                if (jobPushRequest != null) {
                                    LOGGER.info("Get new job :{}", jobPushRequest.getTaskMeta());
                                    returnResponse.setJobMeta(jobPushRequest.getTaskMeta());
                                }
                            }else {
                                LOGGER.info("Job feedback failed, save local files。{}", taskRunResult);
                                try {
                                    retryScheduler.inSchedule(
                                    		taskRunResult.getTaskMeta().getId().concat("_") + SystemClock.now(),
                                    		taskRunResult);
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
            } catch (Exception e) {
                 LOGGER.error("Save files failed, {}", taskRunResult.getTaskMeta(), e);
            }

        }
    }
    /**
     * 发送JobResults
     */
    private boolean retrySendJobResults(List<TaskRunResult> results) {
        // 发送消息给taskDispatch
    	TaskCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new TaskCompletedRequest());
        requestBody.setTaskRunResults(results);
        requestBody.setReSend(true);

        int requestCode = TaskProtos.RequestCode.TASK_COMPLETED.code();
        RemotingCommand request = RemotingCommand.createRequestCommand(requestCode, requestBody);

        try {
        	appContext.getMasterNode();
            // 这里一定要用同步，不然异步会发生文件锁，死锁
        	RemotingClient remotingClient = ServiceLoader.load(RemotingTransporter.class, appContext.getConfig()).getRemotingClient(appContext, new RemotingClientConfig());
        	Channel channel = remotingClient.getAndCreateChannel(appContext.getMasterNode().getAddress());
            RemotingCommand commandResponse = remotingServer.invokeSync(channel,request);
            if (commandResponse != null && commandResponse.getCode() == RemotingProtos.ResponseCode.SUCCESS.code()) {
                return true;
            } else {
                LOGGER.warn("Send job failed, {}", commandResponse);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Retry send job result failed! taskResults={}", results, e);
        }
        return false;
    }
}