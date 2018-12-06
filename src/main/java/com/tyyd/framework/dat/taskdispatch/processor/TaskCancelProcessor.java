package com.tyyd.framework.dat.taskdispatch.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.JobCancelRequest;
import com.tyyd.framework.dat.core.support.JobDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.JobPo;
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

        JobCancelRequest jobCancelRequest = request.getBody();

        String taskId = jobCancelRequest.getTaskId();
        String taskTrackerNodeGroup = jobCancelRequest.getTaskTrackerNodeGroup();
        JobPo job = appContext.getCronJobQueue().getJob(taskTrackerNodeGroup, taskId);
        if (job == null) {
            job = appContext.getExecutableJobQueue().getJob(taskTrackerNodeGroup, taskId);
        }

        if (job != null) {
            appContext.getExecutableJobQueue().remove(job.getTaskTrackerNodeGroup(), job.getJobId());
            if (job.isCron()) {
                appContext.getCronJobQueue().remove(job.getJobId());
            }
            // 记录日志
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(job);
            jobLogPo.setSuccess(true);
            jobLogPo.setLogType(LogType.DEL);
            jobLogPo.setLogTime(SystemClock.now());
            jobLogPo.setLevel(Level.INFO);
            appContext.getJobLogger().log(jobLogPo);

            LOGGER.info("Cancel Job success , jobId={}, taskId={}, taskTrackerNodeGroup={}", job.getJobId(), taskId, taskTrackerNodeGroup);
            return RemotingCommand.createResponseCommand(JobProtos
                    .ResponseCode.JOB_CANCEL_SUCCESS.code());
        }

        return RemotingCommand.createResponseCommand(JobProtos
                .ResponseCode.JOB_CANCEL_FAILED.code(), "Job maybe running");
    }
}
