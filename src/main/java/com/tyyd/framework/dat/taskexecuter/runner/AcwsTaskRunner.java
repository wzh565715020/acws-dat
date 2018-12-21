package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.taskexecuter.Result;

public class AcwsTaskRunner implements TaskRunner {
	private static final Logger LOGGER = LoggerFactory.getLogger(AcwsTaskRunner.class);

	private AcwsTask acwsTask;

	public AcwsTaskRunner(AcwsTask acwsTask) {
		this.acwsTask = acwsTask;
	}

	@Override
	public Result run(Task task) throws Throwable {
		try {
			// 1.任务开始事件
			acwsTask.onTaskStarted(task);

			// 2.任务执行入口
			acwsTask.doTask(task);

			// 3.任务成功完成事件
			acwsTask.onTaskCompletedSucceeded(task);

		} catch (Exception e) {
			try {
				acwsTask.onTaskCompletedFailed(task);
			} catch (Throwable e1) {
			}
			LOGGER.error("任务执行失败", e);
			return new Result(Action.EXECUTE_EXCEPTION, e.getMessage());
		}
		return new Result(Action.EXECUTE_SUCCESS, "任务" +task.getTaskId()+ "执行成功");
	}

}
