package com.tyyd.framework.dat.taskdispatch.support.checker;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.exception.RemotingSendException;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.JobAskRequest;
import com.tyyd.framework.dat.core.protocol.command.JobAskResponse;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.remoting.RemotingServerDelegate;
import com.tyyd.framework.dat.core.support.JobDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.ResponseFuture;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelWrapper;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;

/**
 * @author Robert HG (254963746@qq.com) on 8/19/14.
 *         死掉的任务
 *         1. 分发出去的，并且执行节点不存在的任务
 *         2. 分发出去，执行节点还在, 但是没有在执行的任务
 */
public class ExecutingDeadTaskChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutingDeadTaskChecker.class);

    private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1, new NamedThreadFactory("LTS-ExecutingJobQueue-Fix-Executor", true));

    private TaskDispatcherAppContext appContext;
    private TaskDispatcherMStatReporter stat;

    public ExecutingDeadTaskChecker(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();
    }

    private AtomicBoolean start = new AtomicBoolean(false);
    private ScheduledFuture<?> scheduledFuture;

    public void start() {
        try {
            if (start.compareAndSet(false, true)) {
                int fixCheckPeriodSeconds = appContext.getConfig().getParameter("jobtracker.executing.job.fix.check.interval.seconds", 30);
                if (fixCheckPeriodSeconds < 5) {
                    fixCheckPeriodSeconds = 5;
                } else if (fixCheckPeriodSeconds > 5 * 60) {
                    fixCheckPeriodSeconds = 5 * 60;
                }

                scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
                            if (!appContext.getRegistryStatMonitor().isAvailable()) {
                                return;
                            }
                            checkAndFix();
                        } catch (Throwable t) {
                            LOGGER.error("Check executing dead job error ", t);
                        }
                    }
                }, fixCheckPeriodSeconds, fixCheckPeriodSeconds, TimeUnit.SECONDS);
            }
            LOGGER.info("Executing dead job checker started!");
        } catch (Throwable e) {
            LOGGER.error("Executing dead job checker start failed!", e);
        }
    }

    private void checkAndFix() throws RemotingSendException {

        // 30s没有收到反馈信息，需要去检查这个任务是否还在执行
        int maxDeadCheckTime = appContext.getConfig().getParameter("jobtracker.executing.job.fix.deadline.seconds", 20);
        if (maxDeadCheckTime < 10) {
            maxDeadCheckTime = 10;
        } else if (maxDeadCheckTime > 5 * 60) {
            maxDeadCheckTime = 5 * 60;
        }

        // 查询出所有死掉的任务 (其实可以直接在数据库中fix的, 查询出来主要是为了日志打印)
        // 一般来说这个是没有多大的，我就不分页去查询了
        List<TaskPo> maybeDeadJobPos = appContext.getExecutingJobQueue().getDeadJobs(
                SystemClock.now() - maxDeadCheckTime * 1000);
        if (CollectionUtils.isNotEmpty(maybeDeadJobPos)) {

            Map<String/*taskTrackerIdentity*/, List<TaskPo>> jobMap = new HashMap<String, List<TaskPo>>();
            for (TaskPo jobPo : maybeDeadJobPos) {
                List<TaskPo> jobPos = jobMap.get(jobPo.getTaskTrackerIdentity());
                if (jobPos == null) {
                    jobPos = new ArrayList<TaskPo>();
                    jobMap.put(jobPo.getTaskTrackerIdentity(), jobPos);
                }
                jobPos.add(jobPo);
            }

            for (Map.Entry<String, List<TaskPo>> entry : jobMap.entrySet()) {
                String taskTrackerNodeGroup = entry.getValue().get(0).getTaskTrackerNodeGroup();
                String taskExecuterIdentity = entry.getKey();
                // 去查看这个TaskTrackerIdentity是否存活
                ChannelWrapper channelWrapper = appContext.getChannelManager().getChannel(taskTrackerNodeGroup, NodeType.TASK_EXECUTER, taskExecuterIdentity);
                if (channelWrapper == null && taskExecuterIdentity != null) {
                    Long offlineTimestamp = appContext.getChannelManager().getOfflineTimestamp(taskExecuterIdentity);
                    // 已经离线太久，直接修复
                    if (offlineTimestamp == null || SystemClock.now() - offlineTimestamp > Constants.DEFAULT_TASK_EXECUTER_OFFLINE_LIMIT_MILLIS) {
                        // fixDeadJob
                        fixDeadJob(entry.getValue());
                    }
                } else {
                    // 去询问是否在执行该任务
                    if (channelWrapper != null && channelWrapper.getChannel() != null && channelWrapper.isOpen()) {
                        askTimeoutJob(channelWrapper.getChannel(), entry.getValue());
                    }
                }
            }
        }
    }

    /**
     * 向taskTracker询问执行中的任务
     */
    private void askTimeoutJob(Channel channel, final List<TaskPo> jobPos) {
        try {
            RemotingClientDelegate remotingServer = appContext.getRemotingServer();
            List<String> jobIds = new ArrayList<String>(jobPos.size());
            for (TaskPo jobPo : jobPos) {
                jobIds.add(jobPo.getJobId());
            }
            JobAskRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new JobAskRequest());
            requestBody.setJobIds(jobIds);
            RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.TASK_ASK.code(), requestBody);
            remotingServer.invokeAsync(request, new AsyncCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    RemotingCommand response = responseFuture.getResponseCommand();
                    if (response != null && RemotingProtos.ResponseCode.SUCCESS.code() == response.getCode()) {
                        JobAskResponse responseBody = response.getBody();
                        List<String> deadJobIds = responseBody.getJobIds();
                        if (CollectionUtils.isNotEmpty(deadJobIds)) {
                            try {
                                // 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
                                Thread.sleep(1000L);
                            } catch (InterruptedException ignored) {
                            }
                            for (TaskPo jobPo : jobPos) {
                                if (deadJobIds.contains(jobPo.getJobId())) {
                                    fixDeadJob(jobPo);
                                }
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("Ask timeout Job error, ", e);
        }

    }

    private void fixDeadJob(List<TaskPo> jobPos) {
        for (TaskPo jobPo : jobPos) {
            fixDeadJob(jobPo);
        }
    }

    private void fixDeadJob(TaskPo jobPo) {
        try {

            jobPo.setGmtModified(SystemClock.now());
            jobPo.setTaskTrackerIdentity(null);
            jobPo.setIsRunning(false);
            // 1. add to executable queue
            try {
                appContext.getExecutableJobQueue().add(jobPo);
            } catch (DupEntryException e) {
                LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
            }

            // 2. remove from executing queue
            appContext.getExecutingJobQueue().remove(jobPo.getJobId());

            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
            jobLogPo.setSuccess(true);
            jobLogPo.setLevel(Level.WARN);
            jobLogPo.setLogType(LogType.FIXED_DEAD);
            appContext.getJobLogger().log(jobLogPo);

            stat.incFixExecutingJobNum();

        } catch (Throwable t) {
            LOGGER.error(t.getMessage(), t);
        }
        LOGGER.info("checkAndFix dead job ! {}", JSON.toJSONString(jobPo));
    }

    public void stop() {
        try {
            if (start.compareAndSet(true, false)) {
                scheduledFuture.cancel(true);
                FIXED_EXECUTOR_SERVICE.shutdown();
            }
            LOGGER.info("Executing dead job checker stopped!");
        } catch (Throwable t) {
            LOGGER.error("Executing dead job checker stop failed!", t);
        }
    }

}
