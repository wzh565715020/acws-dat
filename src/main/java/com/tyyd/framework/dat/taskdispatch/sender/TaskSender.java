package com.tyyd.framework.dat.taskdispatch.sender;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
import com.tyyd.framework.dat.store.transaction.SpringContextHolder;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskSender {

	private final Logger LOGGER = LoggerFactory.getLogger(TaskSender.class);

	private TaskDispatcherAppContext appContext;

	public TaskSender(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	public SendResult send(PreLoader preLoader, String taskExecuterIdentity, SendInvoker invoker) {
		// 取一个可运行的task
		final TaskPo taskPo = preLoader.take();
		if (taskPo == null) {
			LOGGER.info("Task push failed: no Task!identity:" + taskExecuterIdentity + "    poolId:"
					+ preLoader.getPoolId());
			return new SendResult(false, TaskPushResult.NO_TASK);
		}
		if (!preLoader.lockTask(taskPo.getId(), taskExecuterIdentity)) {
			return new SendResult(false, TaskPushResult.LOCK_FAIL);
		}
		ApplicationContext applicationContext = SpringContextHolder.getApplicationContext();
		DataSourceTransactionManager dataSourceTransactionManager = applicationContext
				.getBean(DataSourceTransactionManager.class);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 事物隔离级别，开启新事务
		TransactionStatus status = dataSourceTransactionManager.getTransaction(def); // 获得事务状态
		SendResult sendResult = null;
		try {
			PoolQueueReq poolQueueReq = new PoolQueueReq();
			poolQueueReq.setPoolId(taskPo.getPoolId());
			poolQueueReq.setChangeAvailableCount(-1);
			if (!appContext.getPoolQueue().decreaseAvailableCount(poolQueueReq)) {
				return new SendResult(false, TaskPushResult.NO_POOL);
			}
			// IMPORTANT: 切换队列
			taskPo.setPoolId(taskPo.getPoolId());
			taskPo.setUpdateDate(SystemClock.now());
			taskPo.setTaskExecuteNode(taskExecuterIdentity);
			taskPo.setIsRunning(1);
			appContext.getExecutingTaskQueue().add(taskPo);
			if (!taskPo.isCronExpression() && !taskPo.isRepeatableExpression()) {
				appContext.getExecutableTaskQueue().remove(taskPo.getId());
			}
			sendResult = invoker.invoke(taskPo);

			if (sendResult != null && sendResult.isSuccess()) {
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
				dataSourceTransactionManager.commit(status);
			} else {
				dataSourceTransactionManager.rollback(status);
			}

		} catch (Exception e) {
			dataSourceTransactionManager.rollback(status);
			LOGGER.error("处理执行结果失败", e);
		}finally {
			if (!status.isCompleted()) {
				dataSourceTransactionManager.commit(status);
			}
		}
		
		if (sendResult == null) {
			sendResult = new TaskSender.SendResult(false, TaskPushResult.SENT_ERROR);
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
