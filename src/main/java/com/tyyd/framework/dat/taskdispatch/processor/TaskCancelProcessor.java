package com.tyyd.framework.dat.taskdispatch.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskCancelRequest;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskCancelProcessor extends AbstractRemotingProcessor {

    private final Logger LOGGER = LoggerFactory.getLogger(TaskCancelProcessor.class);

    public TaskCancelProcessor(TaskDispatcherAppContext appContext) {
        super(appContext);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        TaskCancelRequest jobCancelRequest = request.getBody();

        String taskId = jobCancelRequest.getTaskId();
        String taskTrackerNodeGroup = jobCancelRequest.getTaskTrackerNodeGroup();
        TaskPo job = appContext.getTaskQueue().getTask(taskId);
        if (job == null) {
            job = appContext.getExecutableTaskQueue().getTask(taskId);
        }

        if (job != null) {
            appContext.getExecutableTaskQueue().remove(job.getTaskId());
            if (job.isCron()) {
                appContext.getTaskQueue().remove(job.getTaskId());
            }
            // 记录日志
            TaskLogPo jobLogPo = TaskDomainConverter.convertJobLog(job);
            jobLogPo.setSuccess(true);
            jobLogPo.setLogType(LogType.DEL);
            jobLogPo.setLogTime(SystemClock.now());
            jobLogPo.setLevel(Level.INFO);
            appContext.getTaskLogger().log(jobLogPo);

            LOGGER.info("Cancel Job success , jobId={}, taskId={}, taskTrackerNodeGroup={}", job.getTaskId(), taskId, taskTrackerNodeGroup);
            return RemotingCommand.createResponseCommand(TaskProtos
                    .ResponseCode.TASK_CANCEL_SUCCESS.code());
        }

        return RemotingCommand.createResponseCommand(TaskProtos
                .ResponseCode.TASK_CANCEL_FAILED.code(), "Job maybe running");
    }
}
