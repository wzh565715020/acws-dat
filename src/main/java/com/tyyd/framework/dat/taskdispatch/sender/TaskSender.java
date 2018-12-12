package com.tyyd.framework.dat.taskdispatch.sender;


import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskSender {

    private final Logger LOGGER = LoggerFactory.getLogger(TaskSender.class);

    private TaskDispatcherAppContext appContext;

    public TaskSender(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    public SendResult send(String taskExecuterIdentity, SendInvoker invoker) {

        //取一个可运行的task
        final TaskPo taskPo = appContext.getPreLoader().take(taskExecuterIdentity);
        if (taskPo == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Task push failed: no Task!identity " + taskExecuterIdentity);
            }
            return new SendResult(false, TaskPushResult.NO_JOB);
        }
        // IMPORTANT: 这里要先切换队列
        try {
            taskPo.setCreateDate(taskPo.getCreateDate());
            appContext.getExecutingJobQueue().add(taskPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutingJobQueue already exist:" + JSON.toJSONString(taskPo));
            appContext.getExecutableJobQueue().resume(taskPo);
            return new SendResult(false, TaskPushResult.FAILED);
        }
        appContext.getExecutableJobQueue().remove(taskPo.getId());

        SendResult sendResult = invoker.invoke(taskPo);

        if (sendResult.isSuccess()) {
            // 记录日志
            JobLogPo jobLogPo = TaskDomainConverter.convertJobLog(taskPo);
            jobLogPo.setSuccess(true);
            jobLogPo.setLogType(LogType.SENT);
            jobLogPo.setLogTime(SystemClock.now());
            jobLogPo.setLevel(Level.INFO);
            appContext.getJobLogger().log(jobLogPo);
        }

        return sendResult;
    }

    public interface SendInvoker {
        SendResult invoke(TaskPo jobPo);
    }

    public static class SendResult {
        private boolean success;
        private Object returnValue;

        public SendResult(boolean success, Object returnValue) {
            this.success = success;
            this.returnValue = returnValue;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public Object getReturnValue() {
            return returnValue;
        }

        public void setReturnValue(Object returnValue) {
            this.returnValue = returnValue;
        }
    }

}
