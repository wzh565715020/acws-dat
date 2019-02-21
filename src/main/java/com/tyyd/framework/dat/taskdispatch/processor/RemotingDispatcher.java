package com.tyyd.framework.dat.taskdispatch.processor;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.tyyd.framework.dat.core.commons.concurrent.limiter.RateLimiter;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.TaskProtos.RequestCode;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;


/**
 *  总的处理器, 每一种命令对应不同的处理器
 */
public class RemotingDispatcher extends AbstractRemotingProcessor {

    private final Map<RequestCode, RemotingProcessor> processors = new HashMap<RequestCode, RemotingProcessor>();
    private RateLimiter rateLimiter;
    private int reqLimitAcquireTimeout = 50;
    private boolean reqLimitEnable = false;

    public RemotingDispatcher(TaskDispatcherAppContext appContext) {
        super(appContext);
        processors.put(RequestCode.SUBMIT_TASK, new TaskSubmitProcessor(appContext));
        processors.put(RequestCode.TASK_COMPLETED, new TaskCompletedProcessor(appContext));
        processors.put(RequestCode.BIZ_LOG_SEND, new TaskBizLogProcessor(appContext));
        processors.put(RequestCode.CANCEL_TASK, new TaskCancelProcessor(appContext));

        this.reqLimitEnable = appContext.getConfig().getParameter("remoting.req.limit.enable", false);
        Integer maxQPS = appContext.getConfig().getParameter("remoting.req.limit.maxQPS", 5000);
        this.rateLimiter = RateLimiter.create(maxQPS);
        this.reqLimitAcquireTimeout = appContext.getConfig().getParameter("remoting.req.limit.acquire.timeout", 50);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {
        // 心跳
        if (request.getCode() == TaskProtos.RequestCode.HEART_BEAT.code()) {
            return RemotingCommand.createResponseCommand(TaskProtos.ResponseCode.HEART_BEAT_SUCCESS.code(), "");
        }
        if (reqLimitEnable) {
            return doBizWithReqLimit(channel, request);
        } else {
            return doBiz(channel, request);
        }
    }

    /**
     * 限流处理
     */
    private RemotingCommand doBizWithReqLimit(Channel channel, RemotingCommand request) throws RemotingCommandException {

        if (rateLimiter.tryAcquire(reqLimitAcquireTimeout, TimeUnit.MILLISECONDS)) {
            return doBiz(channel, request);
        }
        return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SYSTEM_BUSY.code(), "remoting server is busy!");
    }

    private RemotingCommand doBiz(Channel channel, RemotingCommand request) throws RemotingCommandException {
        // 其他的请求code
        RequestCode code = RequestCode.valueOf(request.getCode());
        RemotingProcessor processor = processors.get(code);
        if (processor == null) {
            return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.REQUEST_CODE_NOT_SUPPORTED.code(), "request code not supported!");
        }
        return processor.processRequest(channel, request);
    }
}
