package com.tyyd.framework.dat.taskdispatch.complete.biz;


import java.util.List;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.protocol.command.TaskCompletedRequest;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;

/**
 * 任务数据统计 Chain
 */
public class TaskStatBiz implements TaskCompletedBiz {

    private final Logger LOGGER = LoggerFactory.getLogger(TaskStatBiz.class);

    private TaskDispatcherAppContext appContext;
    private TaskDispatcherMStatReporter stat;

    public TaskStatBiz(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();

    }

    @Override
    public RemotingCommand doBiz(TaskCompletedRequest request) {

        List<TaskRunResult> results = request.getTaskRunResults();

        if (CollectionUtils.isEmpty(results)) {
            return RemotingCommand.createResponseCommand(RemotingProtos
                            .ResponseCode.REQUEST_PARAM_ERROR.code(),
                    "TaskResults can not be empty!");
        }

        LOGGER.info("task execute completed : {}", results);

        LogType logType = request.isReSend() ? LogType.RESEND : LogType.FINISHED;

        for (TaskRunResult result : results) {

            // 记录日志
            TaskLogPo jobLogPo = TaskDomainConverter.convertTaskLog(result.getTaskMeta());
            jobLogPo.setMsg(result.getMsg());
            jobLogPo.setLogType(logType);
            jobLogPo.setSuccess(Action.EXECUTE_SUCCESS.equals(result.getAction()));
            jobLogPo.setTaskExecuteNode(request.getIdentity());
            jobLogPo.setLevel(Level.INFO);
            jobLogPo.setLogTime(result.getTime());
            appContext.getTaskLogger().log(jobLogPo);

            // 监控数据统计
            if (result.getAction() != null) {
                switch (result.getAction()) {
                    case EXECUTE_SUCCESS:
                        stat.incExeSuccessNum();
                        break;
                    case EXECUTE_FAILED:
                        stat.incExeFailedNum();
                        break;
                    case EXECUTE_LATER:
                        stat.incExeLaterNum();
                        break;
                    case EXECUTE_EXCEPTION:
                        stat.incExeExceptionNum();
                        break;
                }
            }
        }
        return null;
    }

}
