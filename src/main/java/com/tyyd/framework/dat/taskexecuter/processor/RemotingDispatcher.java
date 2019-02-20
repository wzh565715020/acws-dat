package com.tyyd.framework.dat.taskexecuter.processor;


import java.util.HashMap;
import java.util.Map;

import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

/**
 *  总的处理器, 每一种命令对应不同的处理器
 */
public class RemotingDispatcher extends AbstractProcessor {

    private final Map<TaskProtos.RequestCode, RemotingProcessor> processors = new HashMap<TaskProtos.RequestCode, RemotingProcessor>();

    public RemotingDispatcher(TaskExecuterAppContext appContext) {
        super(appContext);
        processors.put(TaskProtos.RequestCode.PUSH_TASK, new TaskProcessor(appContext));
        processors.put(TaskProtos.RequestCode.TASK_ASK, new TaskAskProcessor(appContext));
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        TaskProtos.RequestCode code = TaskProtos.RequestCode.valueOf(request.getCode());
        // 心跳
        if (request.getCode() == TaskProtos.RequestCode.HEART_BEAT.code()) {
            return RemotingCommand.createResponseCommand(TaskProtos.ResponseCode.HEART_BEAT_SUCCESS.code(), "");
        }
        RemotingProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(),
                    "request code not supported!");
        }
        return processor.processRequest(channel, request);
    }
}
