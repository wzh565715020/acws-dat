package com.tyyd.framework.dat.taskexecuter.logger;


import java.util.Collections;
import java.util.List;

import com.tyyd.framework.dat.core.commons.utils.Callable;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.domain.BizLog;
import com.tyyd.framework.dat.core.domain.Pair;
import com.tyyd.framework.dat.core.exception.JobTrackerNotFoundException;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.BizLogSendRequest;
import com.tyyd.framework.dat.core.protocol.command.CommandBodyWrapper;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.support.NodeShutdownHook;
import com.tyyd.framework.dat.core.support.RetryScheduler;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.ResponseFuture;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

/**
 * 业务日志记录器实现
 * 1. 业务日志会发送给JobTracker
 * 2. 也会采取Fail And Store 的方式
 *
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class BizLoggerImpl extends BizLoggerAdapter implements BizLogger {

    private Level level;
    private RemotingClientDelegate remotingClient;
    private TaskExecuterAppContext appContext;
    private final ThreadLocal<Pair<String, String>> jobTL;
    private RetryScheduler<BizLog> retryScheduler;

    public BizLoggerImpl(Level level, final RemotingClientDelegate remotingClient, TaskExecuterAppContext appContext) {
        this.level = level;
        if (this.level == null) {
            this.level = Level.INFO;
        }
        this.appContext = appContext;
        this.remotingClient = remotingClient;
        this.jobTL = new ThreadLocal<Pair<String, String>>();
        String storePath = getStorePath();
        this.retryScheduler = new RetryScheduler<BizLog>(appContext, storePath) {
            @Override
            protected boolean isRemotingEnable() {
                return remotingClient.isServerEnable();
            }

            @Override
            protected boolean retry(List<BizLog> list) {
                return sendBizLog(list);
            }
        };
        retryScheduler.setName(BizLogger.class.getSimpleName());
        this.retryScheduler.start();

        NodeShutdownHook.registerHook(appContext, this.getClass().getName(), new Callable() {
            @Override
            public void call() throws Exception {
                retryScheduler.stop();
            }
        });
    }

    private String getStorePath() {
        return appContext.getConfig().getDataPath()
                + "/.lts" + "/" +
                appContext.getConfig().getNodeType() + "/" +
                appContext.getConfig().getNodeGroup() + "/bizlog/";
    }

    public void setId(String jobId, String taskId) {
        jobTL.set(new Pair<String, String>(jobId, taskId));
    }

    public void removeId() {
        jobTL.remove();
    }

    @Override
    public void debug(String msg) {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            sendMsg(msg);
        }
    }

    @Override
    public void info(String msg) {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            sendMsg(msg);
        }
    }

    @Override
    public void error(String msg) {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            sendMsg(msg);
        }
    }

    private void sendMsg(String msg) {

        BizLogSendRequest requestBody = CommandBodyWrapper.wrapper(appContext, new BizLogSendRequest());

        final BizLog bizLog = new BizLog();
        bizLog.setTaskTrackerIdentity(requestBody.getIdentity());
        bizLog.setTaskTrackerNodeGroup(requestBody.getNodeGroup());
        bizLog.setLogTime(SystemClock.now());
        bizLog.setJobId(jobTL.get().getKey());
        bizLog.setTaskId(jobTL.get().getValue());
        bizLog.setMsg(msg);
        bizLog.setLevel(level);

        requestBody.setBizLogs(Collections.singletonList(bizLog));

        if (!remotingClient.isServerEnable()) {
            retryScheduler.inSchedule(StringUtils.generateUUID(), bizLog);
            return;
        }

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.BIZ_LOG_SEND.code(), requestBody);
        try {
            // 有可能down机，日志丢失
            remotingClient.invokeAsync(request, new AsyncCallback() {
                @Override
                public void operationComplete(ResponseFuture responseFuture) {
                    RemotingCommand response = responseFuture.getResponseCommand();

                    if (response != null && response.getCode() == JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code()) {
                        // success
                    } else {
                        retryScheduler.inSchedule(StringUtils.generateUUID(), bizLog);
                    }
                }
            });
        } catch (JobTrackerNotFoundException e) {
            retryScheduler.inSchedule(StringUtils.generateUUID(), bizLog);
        }
    }

    private boolean sendBizLog(List<BizLog> bizLogs) {
        if (CollectionUtils.isEmpty(bizLogs)) {
            return true;
        }
        BizLogSendRequest requestBody = CommandBodyWrapper.wrapper(appContext, new BizLogSendRequest());
        requestBody.setBizLogs(bizLogs);

        RemotingCommand request = RemotingCommand.createRequestCommand(JobProtos.RequestCode.BIZ_LOG_SEND.code(), requestBody);
        try {
            RemotingCommand response = remotingClient.invokeSync(request);
            if (response != null && response.getCode() == JobProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code()) {
                // success
                return true;
            }
        } catch (JobTrackerNotFoundException ignored) {
        }
        return false;
    }

}
