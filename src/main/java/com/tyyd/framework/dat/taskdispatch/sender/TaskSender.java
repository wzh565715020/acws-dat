package com.tyyd.framework.dat.taskdispatch.sender;


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
import com.tyyd.framework.dat.queue.PreLoader;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskSender {

	private final Logger LOGGER = LoggerFactory.getLogger(TaskSender.class);

	private TaskDispatcherAppContext appContext;

	public TaskSender(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	@Transactional(isolation=Isolation.READ_COMMITTED,propagation=Propagation.REQUIRED,rollbackFor = Exception.class)
	public SendResult send(PreLoader preLoader, String taskExecuterIdentity, SendInvoker invoker) {
		// 取一个可运行的task
		final TaskPo taskPo = preLoader.take();
		if (taskPo == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Task push failed: no Task!identity " + taskExecuterIdentity);
			}
			return new SendResult(false, TaskPushResult.NO_TASK);
		}
		PoolQueueReq poolQueueReq = new PoolQueueReq();
		poolQueueReq.setPoolId(taskPo.getPoolId());
		poolQueueReq.setChangeAvailableCount(-1);
		if (!appContext.getPoolQueue().decreaseAvailableCount(poolQueueReq)) {
			return new SendResult(false, TaskPushResult.NO_POOL);
		}
		if (preLoader.lockTask(taskPo.getId(), taskExecuterIdentity)) {
			taskPo.setTaskExecuteNode(taskExecuterIdentity);
			taskPo.setUpdateDate(SystemClock.now());
		}
		// IMPORTANT: 切换队列
		taskPo.setPoolId(taskPo.getPoolId());
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
