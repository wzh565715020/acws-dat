/**
 * 单次执行任务基类
 */
package com.tyyd.framework.dat.taskexecuter.runner;

import org.springframework.stereotype.Service;

import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;

@Service("defaultAcwsTask")
public class AcwsDefaultTask implements AcwsTask {
	private static final Logger LOGGER = LoggerFactory.getLogger(AcwsDefaultTask.class);

	@Override
	public void onTaskStarted(Task taskInfo) {
	}

	@Override
	public void onTaskCompletedSucceeded(Task event) {

	}

	@Override
	public void onTaskCompletedFailed(Task event) {
	}

	@Override
	public void doTask(Task taskInfo) {
		LOGGER.info("默认执行" + taskInfo.getParams());
	}

}
