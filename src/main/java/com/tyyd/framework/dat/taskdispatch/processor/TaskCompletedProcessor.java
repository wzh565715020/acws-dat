package com.tyyd.framework.dat.taskdispatch.processor;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.tyyd.framework.dat.core.protocol.command.JobCompletedRequest;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskdispatch.complete.biz.TaskCompletedBiz;
import com.tyyd.framework.dat.taskdispatch.complete.biz.TaskProcBiz;
import com.tyyd.framework.dat.taskdispatch.complete.biz.TaskStatBiz;
import com.tyyd.framework.dat.taskdispatch.complete.biz.PushNewJobBiz;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 *         TaskTracker 完成任务 的处理器
 */
public class TaskCompletedProcessor extends AbstractRemotingProcessor {

    private List<TaskCompletedBiz> bizChain;

    public TaskCompletedProcessor(final TaskDispatcherAppContext appContext) {
        super(appContext);

        this.bizChain = new CopyOnWriteArrayList<TaskCompletedBiz>();
        this.bizChain.add(new TaskStatBiz(appContext));        // 统计
        this.bizChain.add(new TaskProcBiz(appContext));          // 完成处理
        //this.bizChain.add(new PushNewJobBiz(appContext));           // 获取新任务
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request)
            throws RemotingCommandException {

        JobCompletedRequest requestBody = request.getBody();

        for (TaskCompletedBiz biz : bizChain) {
            RemotingCommand remotingCommand = biz.doBiz(requestBody);
            if (remotingCommand != null) {
                return remotingCommand;
            }
        }
        return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code());
    }

}
