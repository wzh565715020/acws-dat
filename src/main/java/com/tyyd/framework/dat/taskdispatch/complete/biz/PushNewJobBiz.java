package com.tyyd.framework.dat.taskdispatch.complete.biz;

import com.tyyd.framework.dat.core.protocol.command.JobCompletedRequest;
import com.tyyd.framework.dat.core.protocol.command.TaskPushRequest;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;

/**
 * 接受新任务Chain
 *
 */
public class PushNewJobBiz implements TaskCompletedBiz {

    private TaskDispatcherAppContext appContext;

    public PushNewJobBiz(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public RemotingCommand doBiz(JobCompletedRequest request) {
        // 判断是否接受新任务
        if (request.isReceiveNewJob()) {
            try {
                // 查看有没有其他可以执行的任务
                TaskPushRequest jobPushRequest = getNewJob(request.getNodeGroup(), request.getIdentity());
                // 返回 新的任务
                return RemotingCommand.createResponseCommand(RemotingProtos.ResponseCode.SUCCESS.code(), jobPushRequest);
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 获取新任务去执行
     */
    private TaskPushRequest getNewJob(String taskTrackerNodeGroup, String taskTrackerIdentity) {

        TaskSender.SendResult sendResult = appContext.getJobSender().send(taskTrackerIdentity, new TaskSender.SendInvoker() {
            @Override
            public TaskSender.SendResult invoke(TaskPo jobPo) {

                TaskPushRequest jobPushRequest = appContext.getCommandBodyWrapper().wrapper(new TaskPushRequest());
                jobPushRequest.setJobMeta(TaskDomainConverter.convert(jobPo));

                return new TaskSender.SendResult(true, jobPushRequest);
            }
        });

        if (sendResult.isSuccess()) {
            return (TaskPushRequest) sendResult.getReturnValue();
        }
        return null;
    }
}
