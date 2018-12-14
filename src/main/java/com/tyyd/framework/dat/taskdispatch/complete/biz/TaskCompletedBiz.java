package com.tyyd.framework.dat.taskdispatch.complete.biz;

import com.tyyd.framework.dat.core.protocol.command.TaskCompletedRequest;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;

public interface TaskCompletedBiz {

    /**
     * 如果返回空表示继续执行
     */
    RemotingCommand doBiz(TaskCompletedRequest request);

}
