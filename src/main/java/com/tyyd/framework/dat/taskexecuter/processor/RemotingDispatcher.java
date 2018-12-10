package com.tyyd.framework.dat.taskexecuter.processor;


import java.util.HashMap;
import java.util.Map;

import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

/**
 *         task tracker 总的处理器, 每一种命令对应不同的处理器
 */
public class RemotingDispatcher extends AbstractProcessor {

    private final Map<JobProtos.RequestCode, RemotingProcessor> processors = new HashMap<JobProtos.RequestCode, RemotingProcessor>();

    public RemotingDispatcher(TaskExecuterAppContext appContext) {
        super(appContext);
        processors.put(JobProtos.RequestCode.PUSH_TASK, new TaskPushProcessor(appContext));
        processors.put(JobProtos.RequestCode.TASK_ASK, new TaskAskProcessor(appContext));
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        JobProtos.RequestCode code = JobProtos.RequestCode.valueOf(request.getCode());
        RemotingProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                    "request code not supported!");
        }
        return processor.processRequest(channel, request);
    }

}
