package com.tyyd.framework.dat.taskexecuter.processor;


import java.util.List;

import com.tyyd.framework.dat.core.protocol.command.CommandBodyWrapper;
import com.tyyd.framework.dat.core.protocol.command.JobAskRequest;
import com.tyyd.framework.dat.core.protocol.command.JobAskResponse;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

public class TaskAskProcessor extends AbstractProcessor {

    protected TaskAskProcessor(TaskExecuterAppContext appContext) {
        super(appContext);
    }

    @Override
    public RemotingCommand processRequest(Channel channel,
                                          RemotingCommand request) throws RemotingCommandException {

        JobAskRequest requestBody = request.getBody();

        List<String> jobIds = requestBody.getJobIds();

        List<String> notExistJobIds = appContext.getRunnerPool()
                .getRunningJobManager().getNotExists(jobIds);

        JobAskResponse responseBody = CommandBodyWrapper.wrapper(appContext, new JobAskResponse());

        responseBody.setJobIds(notExistJobIds);

        return RemotingCommand.createResponseCommand(
                RemotingProtos.ResponseCode.SUCCESS.code(), "查询成功", responseBody);
    }
}
