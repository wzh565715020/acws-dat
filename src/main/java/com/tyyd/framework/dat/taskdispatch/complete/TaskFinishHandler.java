package com.tyyd.framework.dat.taskdispatch.complete;

import java.util.Date;
import java.util.List;

import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.TaskUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskFinishHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskFinishHandler.class);

    private TaskDispatcherAppContext appContext;

    public TaskFinishHandler(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    public void onComplete(List<TaskRunResult> results) {
        if (CollectionUtils.isEmpty(results)) {
            return;
        }

        for (TaskRunResult result : results) {

            TaskMeta taskMeta = result.getTaskMeta();

            // 当前完成的job是否是重试的
            boolean isRetryForThisTime = "true".equals(taskMeta.getInternalExtParam("isRetry"));

            if (taskMeta.getJob().isCron()) {
                // 是 Cron任务
            	finishTask(taskMeta.getTaskId());
            } else if (taskMeta.getJob().isRepeatable()) {
                finishRepeatJob(taskMeta.getTaskId(), isRetryForThisTime);
            }

            // 从正在执行的队列中移除
            appContext.getExecutingJobQueue().remove(taskMeta.getTaskId());
        }
    }

    private void finishTask(String jobId) {

        TaskPo taskPo = appContext.getTaskQueue().getTask(jobId);
        if (taskPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(taskPo.getCron());
        if (nextTriggerTime == null) {
            // 从CronJob队列中移除
            appContext.getTaskQueue().remove(jobId);
            return;
        }
        // 表示下次还要执行
        try {
            taskPo.setTaskExecuteNode(null);
            taskPo.setTriggerTime(nextTriggerTime.getTime());
            taskPo.setUpdateDate(SystemClock.now());
            appContext.getExecutableJobQueue().add(taskPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(taskPo));
        }
    }

    private void finishRepeatJob(String jobId, boolean isRetryForThisTime) {
        TaskPo jobPo = appContext.getTaskQueue().getTask(jobId);
        if (jobPo == null) {
            // 可能任务队列中改条记录被删除了
            return;
        }
        if (jobPo.getRepeatCount() != -1 && jobPo.getRepeatedCount() >= jobPo.getRepeatCount()) {
            // 已经重试完成, 那么删除
            appContext.getTaskQueue().remove(jobId);
            repeatJobRemoveLog(jobPo);
            return;
        }

        int repeatedCount = jobPo.getRepeatedCount();
        // 如果当前完成的job是重试的,那么不要增加repeatedCount
        if (!isRetryForThisTime) {
            // 更新repeatJob的重复次数
            repeatedCount = appContext.getTaskQueue().incRepeatedCount(jobId);
        }
        if (repeatedCount == -1) {
            // 表示任务已经被删除了
            return;
        }
        long nexTriggerTime = TaskUtils.getRepeatNextTriggerTime(jobPo);
        try {
            jobPo.setRepeatedCount(repeatedCount);
            jobPo.setTaskExecuteNode(null);
            jobPo.setTriggerTime(nexTriggerTime);
            jobPo.setUpdateDate(SystemClock.now());
            appContext.getExecutableJobQueue().add(jobPo);
        } catch (DupEntryException e) {
            LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(jobPo));
        }
    }

    private void repeatJobRemoveLog(TaskPo jobPo) {
        JobLogPo jobLogPo = TaskDomainConverter.convertJobLog(jobPo);
        jobLogPo.setSuccess(true);
        jobLogPo.setLogType(LogType.DEL);
        jobLogPo.setLogTime(SystemClock.now());
        jobLogPo.setLevel(Level.INFO);
        jobLogPo.setMsg("Repeat Job Finished");
        appContext.getJobLogger().log(jobLogPo);
    }

}
