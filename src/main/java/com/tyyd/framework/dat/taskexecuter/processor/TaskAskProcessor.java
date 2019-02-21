package com.tyyd.framework.dat.taskexecuter.processor;

import java.util.ArrayList;
import java.util.List;

import com.tyyd.framework.dat.core.protocol.command.CommandBodyWrapper;
import com.tyyd.framework.dat.core.protocol.command.TaskAskRequest;
import com.tyyd.framework.dat.core.protocol.command.TaskAskResponse;
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
	public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

		TaskAskRequest requestBody = request.getBody();

		List<String> ids = requestBody.getIds();
		
		List<String> newIds = new ArrayList<String>();
		
		List<String> notExistIds = appContext.getRunnerPool().getRunningTaskManager().getNotExists(ids);
		
		if (!notExistIds.isEmpty()) {
			for (String id : notExistIds) {
				if (!appContext.getRetryScheduler().checkValue(id)) {
					newIds.add(id);
				}
			}
		}

		TaskAskResponse responseBody = CommandBodyWrapper.wrapper(appContext, new TaskAskResponse());

		responseBody.setIds(newIds);

		return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code(), "查询成功", responseBody);
	}
}
