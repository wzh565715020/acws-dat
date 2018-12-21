package com.tyyd.framework.dat.taskdispatch.sender;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskSender {

	private final Logger LOGGER = LoggerFactory.getLogger(TaskSender.class);

	private TaskDispatcherAppContext appContext;

	public TaskSender(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	@Transactional(isolation=Isolation.READ_COMMITTED,propagation=Propagation.REQUIRED,rollbackFor = Exception.class)
	public SendResult send(String taskExecuterIdentity, SendInvoker invoker) {

		// 取一个可运行的task
		final TaskPo taskPo = appContext.getPreLoader().take();
		if (taskPo == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Task push failed: no Task!identity " + taskExecuterIdentity);
			}
			return new SendResult(false, TaskPushResult.NO_TASK);
		}
		PoolPo poolPo = getTaskPool(taskPo.getTaskId());
		if (null == poolPo) {
			return new SendResult(false, TaskPushResult.NO_POOL);
		}
		if (appContext.getPreLoader().lockTask(taskPo.getId(), taskExecuterIdentity)) {
			taskPo.setTaskExecuteNode(taskExecuterIdentity);
			taskPo.setUpdateDate(SystemClock.now());
		}
		// IMPORTANT: 切换队列
		taskPo.setPoolId(poolPo.getPoolId());
		taskPo.setCreateDate(taskPo.getCreateDate());
		taskPo.setTaskExecuteNode(taskExecuterIdentity);
		taskPo.setIsRunning(1);
		appContext.getExecutingTaskQueue().add(taskPo);
		appContext.getExecutableTaskQueue().remove(taskPo.getId());

		SendResult sendResult = invoker.invoke(taskPo);

		if (sendResult.isSuccess()) {
			// 记录日志
			try {
				TaskLogPo taskLogPo = TaskDomainConverter.convertTaskLog(taskPo);
				taskLogPo.setSuccess(true);
				taskLogPo.setLogType(LogType.SENT);
				taskLogPo.setLogTime(SystemClock.now());
				taskLogPo.setLevel(Level.INFO);
				appContext.getTaskLogger().log(taskLogPo);
			} catch (Exception e) {
				LOGGER.error("记录日志失败");
			}
			
		}

		return sendResult;
	}

	private PoolPo getTaskPool(String taskId) {
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
			poolQueueReq.setAvailableCount(taskPoolPo.getAvailableCount() - 1);
			taskPoolPo.setAvailableCount(taskPoolPo.getAvailableCount() - 1);
			appContext.getPoolQueue().selectiveUpdate(poolQueueReq);
			return taskPoolPo;
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
