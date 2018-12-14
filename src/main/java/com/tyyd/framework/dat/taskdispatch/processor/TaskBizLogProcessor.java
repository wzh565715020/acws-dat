package com.tyyd.framework.dat.taskdispatch.processor;


import java.util.List;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.domain.BizLog;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.command.BizLogSendRequest;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandException;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskBizLogProcessor extends AbstractRemotingProcessor {

    public TaskBizLogProcessor(TaskDispatcherAppContext appContext) {
        super(appContext);
    }

    @Override
    public RemotingCommand processRequest(Channel channel, RemotingCommand request) throws RemotingCommandException {

        BizLogSendRequest requestBody = request.getBody();

        List<BizLog> bizLogs = requestBody.getBizLogs();
        if (CollectionUtils.isNotEmpty(bizLogs)) {
            for (BizLog bizLog : bizLogs) {
                TaskLogPo jobLogPo = new TaskLogPo();
                jobLogPo.setGmtCreated(SystemClock.now());
                jobLogPo.setLogTime(bizLog.getLogTime());
                jobLogPo.setTaskTrackerNodeGroup(bizLog.getTaskTrackerNodeGroup());
                jobLogPo.setTaskTrackerIdentity(bizLog.getTaskTrackerIdentity());
                jobLogPo.setJobId(bizLog.getJobId());
                jobLogPo.setTaskId(bizLog.getTaskId());
                jobLogPo.setMsg(bizLog.getMsg());
                jobLogPo.setSuccess(true);
                jobLogPo.setLevel(bizLog.getLevel());
                jobLogPo.setLogType(LogType.BIZ);
                appContext.getTaskLogger().log(jobLogPo);
            }
        }

        return RemotingCommand.createResponseCommand(TaskProtos.ResponseCode.BIZ_LOG_SEND_SUCCESS.code(), "");
    }
}
