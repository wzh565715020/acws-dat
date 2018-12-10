package com.tyyd.framework.dat.taskdispatch.support;


import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.core.commons.utils.Holder;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.exception.RequestTimeoutException;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskPullRequest;
import com.tyyd.framework.dat.core.protocol.command.JobPushRequest;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.support.JobDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.ResponseFuture;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.domain.TaskExecuterNode;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;
import com.tyyd.framework.dat.taskdispatch.sender.TaskPushResult;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;

/**
 *         任务分发管理
 */
public class TaskPusher {

    private final Logger LOGGER = LoggerFactory.getLogger(TaskPusher.class);
    private TaskDispatcherAppContext appContext;
    private final ExecutorService executorService;
    private TaskDispatcherMStatReporter stat;
    private RemotingClientDelegate remotingClient;

    public TaskPusher(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.executorService = Executors.newFixedThreadPool(Constants.AVAILABLE_PROCESSOR * 5,
                new NamedThreadFactory(TaskPusher.class.getSimpleName(), true));
        this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();
        this.remotingClient = appContext.getRemotingServer();
    }
    public void concurrentPush() {

        this.executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    push("");
                } catch (Exception e) {
                    LOGGER.error("Job push failed!", e);
                }
            }
        });
    }

    private void push(String nodeGroup) {
    	TaskExecuterNode taskExecuterNode = appContext.getTaskExecuterManager().
                getTaskTrackerNode(nodeGroup);
    	if (taskExecuterNode == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("taskTrackerNodeGroup:{}didn't have node.", nodeGroup);
            }
            return;
        }
        String identity = taskExecuterNode.getIdentity();
        // 更新TaskExecuter的可用线程数
        appContext.getTaskExecuterManager().updateTaskTrackerAvailableThreads(nodeGroup,
                identity, taskExecuterNode.getAvailableThreadInteger(), taskExecuterNode.getTimestamp());

        int availableThreads = taskExecuterNode.getAvailableThread().get();
        if (availableThreads == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , availableThreads:0", nodeGroup, identity);
            }
        }
        while (availableThreads > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , availableThreads:{}",
                        nodeGroup, identity, availableThreads);
            }
            // 推送任务
            TaskPushResult result = send(remotingClient, taskExecuterNode);
            switch (result) {
                case SUCCESS:
                    availableThreads = taskExecuterNode.getAvailableThread().decrementAndGet();
                    stat.incPushJobNum();
                    break;
                case FAILED:
                    // 还是要继续发送
                    break;
                case NO_JOB:
                    // 没有任务了
                    return;
                case SENT_ERROR:
                    // TaskTracker链接失败
                    return;
            }
        }
    }

    public void concurrentPush(final TaskPullRequest request) {

        this.executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    push(request);
                } catch (Exception e) {
                    LOGGER.error("Job push failed!", e);
                }
            }
        });
    }

    private void push(final TaskPullRequest request) {

        String nodeGroup = request.getNodeGroup();
        String identity = request.getIdentity();
        // 更新TaskExecuter的可用线程数
        appContext.getTaskExecuterManager().updateTaskTrackerAvailableThreads(nodeGroup,
                identity, request.getAvailableThreads(), request.getTimestamp());
        TaskExecuterNode taskExecuterNode = appContext.getTaskExecuterManager().
                getTaskTrackerNode(nodeGroup,identity);

        if (taskExecuterNode == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , didn't have node.", nodeGroup, identity);
            }
            return;
        }

        int availableThreads = taskExecuterNode.getAvailableThread().get();
        if (availableThreads == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , availableThreads:0", nodeGroup, identity);
            }
        }
        while (availableThreads > 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , availableThreads:{}",
                        nodeGroup, identity, availableThreads);
            }
            // 推送任务
            TaskPushResult result = send(remotingClient, taskExecuterNode);
            switch (result) {
                case SUCCESS:
                    availableThreads = taskExecuterNode.getAvailableThread().decrementAndGet();
                    stat.incPushJobNum();
                    break;
                case FAILED:
                    // 还是要继续发送
                    break;
                case NO_JOB:
                    // 没有任务了
                    return;
                case SENT_ERROR:
                    // TaskTracker链接失败
                    return;
            }
        }
    }

    /**
     * 是否推送成功
     */
    private TaskPushResult send(final RemotingClientDelegate remotingClient, final TaskExecuterNode taskTrackerNode) {

        final String nodeGroup = taskTrackerNode.getNodeGroup();
        final String identity = taskTrackerNode.getIdentity();

        TaskSender.SendResult sendResult = appContext.getJobSender().send(nodeGroup, identity, new TaskSender.SendInvoker() {
            @Override
            public TaskSender.SendResult invoke(final TaskPo jobPo) {

                // 发送给TaskTracker执行
                JobPushRequest body = appContext.getCommandBodyWrapper().wrapper(new JobPushRequest());
                body.setJobMeta(JobDomainConverter.convert(jobPo));
                RemotingCommand commandRequest = RemotingCommand.createRequestCommand(JobProtos.RequestCode.PUSH_TASK.code(), body);

                // 是否分发推送任务成功
                final Holder<Boolean> pushSuccess = new Holder<Boolean>(false);

                final CountDownLatch latch = new CountDownLatch(1);
                try {
                	remotingClient.invokeAsync(taskTrackerNode.getChannel().getChannel(),commandRequest, new AsyncCallback() {
                        @Override
                        public void operationComplete(ResponseFuture responseFuture) {
                            try {
                                RemotingCommand responseCommand = responseFuture.getResponseCommand();
                                if (responseCommand == null) {
                                    LOGGER.warn("Job push failed! response command is null!");
                                    return;
                                }
                                if (responseCommand.getCode() == JobProtos.ResponseCode.TASK_PUSH_SUCCESS.code()) {
                                    if (LOGGER.isDebugEnabled()) {
                                        LOGGER.debug("Job push success! nodeGroup=" + nodeGroup + ", identity=" + identity + ", job=" + jobPo);
                                    }
                                    pushSuccess.set(true);
                                }
                            } finally {
                                latch.countDown();
                            }
                        }
                    });

                } catch (Exception e) {
                    LOGGER.error("Remoting send error, jobPo={}", jobPo, e);
                    return new TaskSender.SendResult(false, TaskPushResult.SENT_ERROR);
                }

                try {
                    latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RequestTimeoutException(e);
                }

                if (!pushSuccess.get()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Job push failed! nodeGroup=" + nodeGroup + ", identity=" + identity + ", job=" + jobPo);
                    }
                    // 队列切回来
                    boolean needResume = true;
                    try {
                        jobPo.setIsRunning(true);
                        jobPo.setGmtModified(SystemClock.now());
                        appContext.getExecutableJobQueue().add(jobPo);
                    } catch (DupEntryException e) {
                        LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
                        needResume = false;
                    }
                    appContext.getExecutingJobQueue().remove(jobPo.getJobId());
                    if (needResume) {
                        appContext.getExecutableJobQueue().resume(jobPo);
                    }
                    return new TaskSender.SendResult(false, TaskPushResult.SENT_ERROR);
                }

                return new TaskSender.SendResult(true, TaskPushResult.SUCCESS);
            }
        });

        return (TaskPushResult) sendResult.getReturnValue();
    }
}
