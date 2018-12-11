package com.tyyd.framework.dat.taskexecuter.processor;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.core.exception.RequestTimeoutException;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.JobCompletedRequest;
import com.tyyd.framework.dat.core.protocol.command.TaskPushRequest;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
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
public class TaskProcessor extends AbstractProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskProcessor.class);

    private TaskRunnerCallback taskRunnerCallback;
    private RemotingServerDelegate remotingServer;

    protected TaskProcessor(TaskExecuterAppContext appContext) {
        super(appContext);
        this.remotingServer = appContext.getRemotingServer();
        // 线程安全的
        taskRunnerCallback = new TaskRunnerCallback();
    }

    @Override
    public RemotingCommand processRequest(Channel channel,
                                          final RemotingCommand request) throws RemotingCommandException {

        TaskPushRequest requestBody = request.getBody();

        // JobTracker 分发来的 job
        final TaskMeta taskMeta = requestBody.getJobMeta();

        try {
            appContext.getRunnerPool().execute(channel,taskMeta, taskRunnerCallback);
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
    private class TaskRunnerCallback implements RunnerCallback {
        @Override
        public void runComplete(Response response) {
            // 发送消息给 JobTracker
            final TaskRunResult taskRunResult = new TaskRunResult();
            taskRunResult.setTime(SystemClock.now());
            taskRunResult.setTaskMeta(response.getTaskMeta());
            taskRunResult.setAction(response.getAction());
            taskRunResult.setMsg(response.getMsg());
            JobCompletedRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobCompletedRequest());
            requestBody.addJobResult(taskRunResult);
            requestBody.setReceiveNewJob(response.isReceiveNewJob());     // 设置可以接受新任务

            int requestCode = JobProtos.RequestCode.TASK_COMPLETED.code();

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
                                    LOGGER.info("Get new job :{}", jobPushRequest.getJobMeta());
                                    returnResponse.setJobMeta(jobPushRequest.getJobMeta());
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
}
