package com.tyyd.framework.dat.taskdispatch.sender;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.PoolPo;
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

		// 取一个可运行的task
		final TaskPo taskPo = appContext.getPreLoader().take(taskExecuterIdentity);
		if (taskPo == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Task push failed: no Task!identity " + taskExecuterIdentity);
			}
			return new SendResult(false, TaskPushResult.NO_TASK);
		}
		String poolId = getTaskPool(taskPo.getTaskId());
		if (null == poolId) {
			return new SendResult(false, TaskPushResult.NO_POOL);
		}
		if (appContext.getPreLoader().lockTask(taskPo.getId(), taskExecuterIdentity, taskPo.getTriggerTime(), taskPo.getUpdateDate())) {
			taskPo.setTaskExecuteNode(taskExecuterIdentity);
			taskPo.setUpdateDate(SystemClock.now());
		}
		// IMPORTANT: 这里要先切换队列
		try {
			taskPo.setPoolId(poolId);
			taskPo.setCreateDate(taskPo.getCreateDate());
			appContext.getExecutingTaskQueue().add(taskPo);
		} catch (DupEntryException e) {
			LOGGER.warn("ExecutingJobQueue already exist:" + JSON.toJSONString(taskPo));
			appContext.getExecutableTaskQueue().resume(taskPo);
			return new SendResult(false, TaskPushResult.FAILED);
		}
		appContext.getExecutableTaskQueue().remove(taskPo.getId());

		SendResult sendResult = invoker.invoke(taskPo);

		if (sendResult.isSuccess()) {
			// 记录日志
			TaskLogPo taskLogPo = TaskDomainConverter.convertJobLog(taskPo);
			taskLogPo.setSuccess(true);
			taskLogPo.setLogType(LogType.SENT);
			taskLogPo.setLogTime(SystemClock.now());
			taskLogPo.setLevel(Level.INFO);
			appContext.getTaskLogger().log(taskLogPo);
		}

		return sendResult;
	}

	private String getTaskPool(String taskId) {
		ReentrantLock reentrantLock = new ReentrantLock();
		reentrantLock.lock();
		try {
			List<PoolPo> list = appContext.getPoolPoList();
			PoolPo taskPoolPo = null;
			for (PoolPo poolPo : list) {
				if (poolPo.getTaskIds().contains(taskId)) {
					taskPoolPo = poolPo;
					break;
				}
			}
			if (null == taskPoolPo) {
				return null;
			}
			PoolQueueReq poolQueueReq = new PoolQueueReq();
			poolQueueReq.setPoolId(taskPoolPo.getPoolId());
			poolQueueReq.setAvailableCount(taskPoolPo.getAvailableCount() > 0 ? taskPoolPo.getAvailableCount() - 1 : 0);
			appContext.getPoolQueue().selectiveUpdate(poolQueueReq);
			return taskPoolPo.getPoolId();
		} finally {
			reentrantLock.unlock();
		}
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
