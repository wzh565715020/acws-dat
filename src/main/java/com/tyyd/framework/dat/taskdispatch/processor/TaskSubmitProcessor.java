package com.tyyd.framework.dat.taskdispatch.processor;

import com.tyyd.framework.dat.core.exception.TaskReceiveException;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskSubmitRequest;
import com.tyyd.framework.dat.core.protocol.command.TaskSubmitResponse;
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

        TaskSubmitResponse jobSubmitResponse = appContext.getCommandBodyWrapper().wrapper(new TaskSubmitResponse());
        RemotingCommand response;
        try {
            appContext.getTaskReceiver().receive(jobSubmitRequest);

            response = RemotingCommand.createResponseCommand(
                    TaskProtos.ResponseCode.TASK_RECEIVE_SUCCESS.code(), "task submit success!", jobSubmitResponse);

        } catch (TaskReceiveException e) {
            LOGGER.error("Receive task failed , jobs = " + jobSubmitRequest.getTasks(), e);
            jobSubmitResponse.setSuccess(false);
            jobSubmitResponse.setMsg(e.getMessage());
            jobSubmitResponse.setFailedJobs(e.getJobs());
            response = RemotingCommand.createResponseCommand(
                    TaskProtos.ResponseCode.TASK_RECEIVE_FAILED.code(), e.getMessage(), jobSubmitResponse);
        }

        return response;
    }
}
