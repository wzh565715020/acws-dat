package com.tyyd.framework.dat.taskdispatch.processor;

import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.command.JobPullRequest;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.support.TaskPusher;

/**
 *         处理 TaskTracker的 Job pull 请求
 */
public class TaskPullProcessor extends AbstractRemotingProcessor {

    private TaskPusher jobPusher;

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskPullProcessor.class);

    public TaskPullProcessor(TaskDispatcherAppContext appContext) {
        super(appContext);

        jobPusher = new TaskPusher(appContext);
    }

    @Override
    public RemotingCommand processRequest(final Channel ctx, final RemotingCommand request) throws RemotingCommandException {

        JobPullRequest requestBody = request.getBody();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("taskTrackerNodeGroup:{}, taskTrackerIdentity:{} , availableThreads:{}", requestBody.getNodeGroup(), requestBody.getIdentity(), requestBody.getAvailableThreads());
        }
        jobPusher.concurrentPush(requestBody);

        return RemotingCommand.createResponseCommand(JobProtos.ResponseCode.JOB_PULL_SUCCESS.code(), "");
    }
}
