package com.tyyd.framework.dat.taskclient.processor;


import java.util.HashMap;
import java.util.Map;

import com.tyyd.framework.dat.core.protocol.JobProtos;
import com.tyyd.framework.dat.core.protocol.JobProtos.RequestCode;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskclient.domain.TaskClientAppContext;


/**
 *         客户端默认通信处理器
 */
public class RemotingDispatcher implements RemotingProcessor {

    private static final RequestCode JOB_COMPLETED = null;
	private final Map<JobProtos.RequestCode, RemotingProcessor> processors = new HashMap<JobProtos.RequestCode, RemotingProcessor>();

    public RemotingDispatcher(TaskClientAppContext appContext) {
        processors.put(JOB_COMPLETED, new TaskFinishedProcessor(appContext));
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {
        JobProtos.RequestCode code = valueOf(request.getCode());
        RemotingProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(), "request code not supported!");
        }
        return processor.processRequest(channel, request);
    }

	private RequestCode valueOf(int code) {
		return null;
	}
}
