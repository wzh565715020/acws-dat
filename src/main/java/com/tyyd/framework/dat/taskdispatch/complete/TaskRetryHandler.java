package com.tyyd.framework.dat.taskdispatch.complete;


import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.core.domain.TaskRunResult;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.TaskUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskRetryHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRetryHandler.class);

    private TaskDispatcherAppContext appContext;
    private long retryInterval = 30 * 1000;     // 默认30s

    public TaskRetryHandler(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.retryInterval = appContext.getConfig().getParameter("jobtracker.job.retry.interval.millis", 30 * 1000);
    }

    public void onComplete(List<TaskRunResult> results) {

        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        for (TaskRunResult result : results) {

            TaskMeta jobMeta = result.getTaskMeta();
            // 1. 加入到重试队列
            TaskPo taskPo = appContext.getExecutingTaskQueue().getJob(jobMeta.getId());
            if (taskPo == null) {    // 表示已经被删除了
                continue;
            }

            // 重试次数+1
            // 1 分钟重试一次吧
            Long nextRetryTriggerTime = SystemClock.now() + retryInterval;

            if (taskPo.isCron()) {
                // 如果是 cron Job, 判断任务下一次执行时间和重试时间的比较
                TaskPo cronJobPo = appContext.getTaskQueue().getTask(jobMeta.getTask().getTaskId());
                if (cronJobPo != null) {
                    Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(cronJobPo.getCron());
                    if (nextTriggerTime != null && nextTriggerTime.getTime() < nextRetryTriggerTime) {
                        // 表示下次还要执行, 并且下次执行时间比下次重试时间要早, 那么不重试，直接使用下次的执行时间
                        nextRetryTriggerTime = nextTriggerTime.getTime();
                        taskPo = cronJobPo;
                    } 
                }
            } else if (taskPo.isRepeatable()) {
                TaskPo repeatJobPo = appContext.getTaskQueue().getTask(jobMeta.getTask().getTaskId());
                if (repeatJobPo != null) {
                    // 比较下一次重复时间和重试时间
                    if (repeatJobPo.getRepeatCount() == -1 || (repeatJobPo.getRepeatedCount() < repeatJobPo.getRepeatCount())) {
                        long nexTriggerTime = TaskUtils.getRepeatNextTriggerTime(taskPo);
                        if (nexTriggerTime < nextRetryTriggerTime) {
                            // 表示下次还要执行, 并且下次执行时间比下次重试时间要早, 那么不重试，直接使用下次的执行时间
                            nextRetryTriggerTime = nexTriggerTime;
                            taskPo = repeatJobPo;
                        }
                    }
                }
            } else {
            }

            // 加入到队列, 重试
            taskPo.setTaskExecuteNode(null);
            taskPo.setUpdateDate(SystemClock.now());
            // 延迟重试时间就等于重试次数(分钟)
            taskPo.setTriggerTime(nextRetryTriggerTime);
            try {
                appContext.getExecutableTaskQueue().add(taskPo);
            } catch (DupEntryException e) {
                LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(taskPo));
            }
            // 从正在执行的队列中移除
            appContext.getExecutingTaskQueue().remove(taskPo.getId());
        }
    }
}
