package com.tyyd.framework.dat.taskdispatch.complete.biz;


import java.util.List;

import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.core.protocol.command.TaskCompletedRequest;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.complete.TaskFinishHandler;
import com.tyyd.framework.dat.taskdispatch.complete.TaskRetryHandler;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 * 任务完成 
 *
 */
public class TaskProcBiz implements TaskCompletedBiz {

    private final TaskRetryHandler retryHandler;
    private final TaskFinishHandler taskFinishHandler;
    // 任务的最大重试次数
    private final Integer globalMaxRetryTimes;

    public TaskProcBiz(final TaskDispatcherAppContext appContext) {
        this.retryHandler = new TaskRetryHandler(appContext);
        this.taskFinishHandler = new TaskFinishHandler(appContext);
        this.globalMaxRetryTimes = appContext.getConfig().getParameter(Constants.TASK_MAX_RETRY_TIMES,
                Constants.DEFAULT_TASK_MAX_RETRY_TIMES);

    }

    @Override
    public RemotingCommand doBiz(TaskCompletedRequest request) {

        List<TaskRunResult> results = request.getTaskRunResults();
        multiResultsProcess(results);
        return null;
    }
    /**
     * 判断任务是否需要加入重试队列
     */
    private boolean needRetry(TaskRunResult result) {
        // 判断类型
        if (!(Action.EXECUTE_LATER.equals(result.getAction())
                || Action.EXECUTE_EXCEPTION.equals(result.getAction()))) {
            return false;
        }

        // 判断重试次数
        Task task = result.getTaskMeta().getTask();
        Integer retryTimes = task.getRetryTimes();
        int jobMaxRetryTimes = task.getMaxRetryTimes();
        return !(retryTimes >= globalMaxRetryTimes || retryTimes >= jobMaxRetryTimes);
    }

    /**
     * 这里情况一般是发送失败，重新发送的
     */
    private void multiResultsProcess(List<TaskRunResult> results) {

        List<TaskRunResult> retryResults = null;
        // 不需要反馈的
        List<TaskRunResult> finishResults = null;

        for (TaskRunResult result : results) {
            if (needRetry(result)) {
                // 需要加入到重试队列的
                retryResults = CollectionUtils.newArrayListOnNull(retryResults);
                retryResults.add(result);
            } else {
                // 不用反馈客户端，也不用重试，直接完成处理
                finishResults = CollectionUtils.newArrayListOnNull(finishResults);
                finishResults.add(result);
            }
        }

        // 完成任务
        taskFinishHandler.onComplete(finishResults);

        // 将任务加入到重试队列
        retryHandler.onComplete(retryResults);
    }
}
