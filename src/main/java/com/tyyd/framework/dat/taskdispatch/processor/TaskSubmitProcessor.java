package com.tyyd.framework.dat.taskdispatch.processor;

import com.tyyd.framework.dat.core.exception.JobReceiveException;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskSubmitRequest;
import com.tyyd.framework.dat.core.protocol.command.JobSubmitResponse;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 *         客户端提交任务的处理器
 */
public class TaskSubmitProcessor extends AbstractRemotingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskSubmitProcessor.class);

    public TaskSubmitProcessor(TaskDispatcherAppContext appContext) {
        super(appContext);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        TaskSubmitRequest jobSubmitRequest = request.getBody();

        JobSubmitResponse jobSubmitResponse = appContext.getCommandBodyWrapper().wrapper(new JobSubmitResponse());
        RemotingCommand response;
        try {
            appContext.getJobReceiver().receive(jobSubmitRequest);

            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.TASK_RECEIVE_SUCCESS.code(), "job submit success!", jobSubmitResponse);

        } catch (JobReceiveException e) {
            LOGGER.error("Receive job failed , jobs = " + jobSubmitRequest.getJobs(), e);
            jobSubmitResponse.setSuccess(false);
            jobSubmitResponse.setMsg(e.getMessage());
            jobSubmitResponse.setFailedJobs(e.getJobs());
            response = RemotingCommand.createResponseCommand(
                    JobProtos.ResponseCode.TASK_RECEIVE_FAILED.code(), e.getMessage(), jobSubmitResponse);
        }

        return response;
    }
}
