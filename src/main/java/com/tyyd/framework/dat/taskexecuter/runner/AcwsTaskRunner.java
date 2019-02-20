package com.tyyd.framework.dat.taskexecuter.runner;

import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.store.transaction.SpringContextHolder;
import com.tyyd.framework.dat.taskexecuter.Result;

public class AcwsTaskRunner implements TaskRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(AcwsTaskRunner.class);

	private AcwsTask acwsTask;

	public AcwsTaskRunner(AcwsTask acwsTask) {
		this.acwsTask = acwsTask;
	}

	@Override
	public Result run(Task task) throws Throwable {
		ApplicationContext applicationContext = SpringContextHolder.getApplicationContext();
		DataSourceTransactionManager dataSourceTransactionManager = applicationContext
				.getBean(DataSourceTransactionManager.class);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 事物隔离级别，开启新事务，这样会比较安全些。
		TransactionStatus status = dataSourceTransactionManager.getTransaction(def); // 获得事务状态
		try {
			// 1.任务开始事件
			acwsTask.onTaskStarted(task);

			// 2.任务执行入口
			acwsTask.doTask(task);

			// 3.任务成功完成事件
			acwsTask.onTaskCompletedSucceeded(task);
			dataSourceTransactionManager.commit(status);
		} catch (Exception e) {
			dataSourceTransactionManager.rollback(status);
			ApplicationContext applicationContextFail = SpringContextHolder.getApplicationContext();
			DataSourceTransactionManager dataSourceTransactionManagerFail = applicationContextFail
					.getBean(DataSourceTransactionManager.class);
			DefaultTransactionDefinition defFail = new DefaultTransactionDefinition();
			defFail.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 事物隔离级别，开启新事务，这样会比较安全些。
			TransactionStatus statusFail = dataSourceTransactionManagerFail.getTransaction(def); // 获得事务状态
			try {
				acwsTask.onTaskCompletedFailed(task);
				dataSourceTransactionManagerFail.commit(statusFail);
			} catch (Throwable e1) {
				dataSourceTransactionManagerFail.rollback(statusFail);
			}
			LOGGER.error("任务执行失败", e);
			return new Result(Action.EXECUTE_EXCEPTION, e.getMessage());
		}
		return new Result(Action.EXECUTE_SUCCESS, "任务" + task.getTaskId() + "执行成功");
	}

}
