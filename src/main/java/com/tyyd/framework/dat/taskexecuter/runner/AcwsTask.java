package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.core.domain.Task;

public interface AcwsTask {
	
	/**
	 * 执行任务实例-任务执行入口
	 * @param taskInfo ：任务实例参数
	 * @return
	 */
	public void doTask(Task taskInfo);
	
	/**
	 * 任务开始事件
	 * @param taskInfo
	 */
	public void onTaskStarted(Task taskInfo);
	
	/**
	 * 任务成功完成事件
	 * @param event
	 */
	public void onTaskCompletedSucceeded(Task event);
	
	/**
	 * 任务失败事件
	 * @param event
	 */
	public void onTaskCompletedFailed(Task event);

}
