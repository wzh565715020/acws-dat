package com.tyyd.framework.dat.taskdispatch.complete.biz;

import com.tyyd.framework.dat.core.protocol.command.JobCompletedRequest;
import com.tyyd.framework.dat.core.protocol.command.JobPushRequest;
import com.tyyd.framework.dat.core.support.JobDomainConverter;
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
                JobPushRequest jobPushRequest = getNewJob(request.getNodeGroup(), request.getIdentity());
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
    private JobPushRequest getNewJob(String taskTrackerNodeGroup, String taskTrackerIdentity) {

        TaskSender.SendResult sendResult = appContext.getJobSender().send(taskTrackerNodeGroup, taskTrackerIdentity, new TaskSender.SendInvoker() {
            @Override
            public TaskSender.SendResult invoke(TaskPo jobPo) {

                JobPushRequest jobPushRequest = appContext.getCommandBodyWrapper().wrapper(new JobPushRequest());
                jobPushRequest.setJobMeta(JobDomainConverter.convert(jobPo));

                return new TaskSender.SendResult(true, jobPushRequest);
            }
        });

        if (sendResult.isSuccess()) {
            return (JobPushRequest) sendResult.getReturnValue();
        }
        return null;
    }
}
