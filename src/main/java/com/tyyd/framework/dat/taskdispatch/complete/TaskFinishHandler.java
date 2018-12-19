package com.tyyd.framework.dat.taskdispatch.complete;

import java.util.Date;
import java.util.List;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.admin.request.PoolQueueReq;
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
import com.tyyd.framework.dat.queue.domain.PoolPo;
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

			// 当前完成的task是否是重试的
			boolean isRetryForThisTime = "true".equals(taskMeta.getInternalExtParam("isRetry"));

			if (taskMeta.getTask().isCron()) {
				// 是 Cron任务
				finishCronTask(taskMeta.getTask().getTaskId());
			} else if (taskMeta.getTask().isRepeatable()) {
				finishRepeatTask(taskMeta.getTask().getTaskId(), isRetryForThisTime);
			}

			// 从正在执行的队列中移除
			appContext.getExecutingTaskQueue().remove(taskMeta.getId());
			// 修改队列可用数量
			PoolPo poolPo = appContext.getPoolQueue().getPool(taskMeta.getTask().getPoolId());
			PoolQueueReq poolQueueReq = new PoolQueueReq();
			poolQueueReq.setPoolId(taskMeta.getTask().getPoolId());
			poolQueueReq.setAvailableCount(poolPo.getAvailableCount() + 1);
			appContext.getPoolQueue().selectiveUpdate(poolQueueReq);
			// 加入到历史队列
			TaskPo taskPo = TaskDomainConverter.convert(taskMeta.getTask());
			taskPo.setId(taskMeta.getId());
			appContext.getExecutedTaskQueue().add(taskPo);
		}
	}

	private void finishCronTask(String id) {

		TaskPo taskPo = appContext.getTaskQueue().getTask(id);
		if (taskPo == null) {
			// 可能任务队列中改条记录被删除了
			return;
		}
		Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(taskPo.getCron());
		if (nextTriggerTime == null) {
			// 从CronJob队列中移除
			appContext.getTaskQueue().remove(id);
			return;
		}
		// 表示下次还要执行
		try {
			taskPo.setTaskExecuteNode(null);
			taskPo.setTriggerTime(nextTriggerTime.getTime());
			taskPo.setUpdateDate(SystemClock.now());
			appContext.getExecutableTaskQueue().add(taskPo);
		} catch (DupEntryException e) {
			LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(taskPo));
		}
	}

	private void finishRepeatTask(String taskId, boolean isRetryForThisTime) {
		TaskPo taskPo = appContext.getTaskQueue().getTask(taskId);
		if (taskPo == null) {
			// 可能任务队列中改条记录被删除了
			return;
		}
		if (taskPo.getRepeatCount() != -1 && taskPo.getRepeatedCount() >= taskPo.getRepeatCount()) {
			// 已经重试完成, 那么删除
			appContext.getTaskQueue().remove(taskId);
			repeatTaskRemoveLog(taskPo);
			return;
		}

		int repeatedCount = taskPo.getRepeatedCount();
		// 如果当前完成的job是重试的,那么不要增加repeatedCount
		if (!isRetryForThisTime) {
			// 更新repeatJob的重复次数
			repeatedCount = appContext.getTaskQueue().incRepeatedCount(taskId);
		}
		if (repeatedCount == -1) {
			// 表示任务已经被删除了
			return;
		}
		long nexTriggerTime = TaskUtils.getRepeatNextTriggerTime(taskPo);
		try {
			taskPo.setRepeatedCount(repeatedCount);
			taskPo.setTaskExecuteNode(null);
			taskPo.setTriggerTime(nexTriggerTime);
			taskPo.setUpdateDate(SystemClock.now());
			appContext.getExecutableTaskQueue().add(taskPo);
		} catch (DupEntryException e) {
			LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(taskPo));
		}
	}

	private void repeatTaskRemoveLog(TaskPo taskPo) {
		TaskLogPo taskLogPo = TaskDomainConverter.convertJobLog(taskPo);
		taskLogPo.setSuccess(true);
		taskLogPo.setLogType(LogType.DEL);
		taskLogPo.setLogTime(SystemClock.now());
		taskLogPo.setLevel(Level.INFO);
		taskLogPo.setMsg("Repeat task Finished");
		appContext.getTaskLogger().log(taskLogPo);
	}

}
