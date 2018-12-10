package com.tyyd.framework.dat.taskclient.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.JobFinishedRequest;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskclient.domain.TaskClientAppContext;
import com.tyyd.framework.dat.taskclient.support.TaskClientMStatReporter;

public class TaskFinishedProcessor implements RemotingProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskFinishedProcessor.class);

    private TaskClientAppContext appContext;
    private TaskClientMStatReporter stat;

    public TaskFinishedProcessor(TaskClientAppContext appContext) {
        this.appContext = appContext;
        this.stat = (TaskClientMStatReporter) appContext.getMStatReporter();
    }

    @Override
    public RemotingCommand processRequest(Channel Channel, RemotingCommand request)
            throws RemotingCommandException {

        JobFinishedRequest requestBody = request.getBody();
        try {
            if (appContext.getJobCompletedHandler() != null) {
                appContext.getJobCompletedHandler().onComplete(requestBody.getJobResults());
                stat.incHandleFeedbackNum(CollectionUtils.sizeOf(requestBody.getJobResults()));
            }
        } catch (Exception t) {
            LOGGER.error(t.getMessage(), t);
        }

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.TASK_NOTIFY_SUCCESS.code(),
                "received successful");
    }
}
