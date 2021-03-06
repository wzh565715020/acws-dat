package com.tyyd.framework.dat.taskdispatch.support.util;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.ThrowsAdvice;

import com.tyyd.framework.dat.core.constant.RunningEnum;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.domain.TaskExecType;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.PoolQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.MysqlExecutableTaskQueue;
import com.tyyd.framework.dat.queue.mysql.MysqlExecutingTaskQueue;
import com.tyyd.framework.dat.queue.mysql.MysqlPoolQueue;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.id.UUIDGenerator;

/**
 * 任务处理器
 */
public class TaskReceiveUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskReceiveUtil.class);

	private static IdGenerator idGenerator = new UUIDGenerator();

	private static ExecutableTaskQueue executableTaskQueue = new MysqlExecutableTaskQueue();

	private static ExecutingTaskQueue executingTaskQueue = new MysqlExecutingTaskQueue();

	private static PoolQueue poolQueue = new MysqlPoolQueue();

	public static void addToQueue(Task task) throws Exception {
		if (task == null) {
			LOGGER.warn("task can not be null。");
			return;
		}
		TaskPo taskPo = TaskDomainConverter.convert(task);
		// 设置 id
		taskPo.setId(idGenerator.generate());
		// 添加任务
		addTask(taskPo);
	}

	/**
	 * 添加任务
	 */
	public static void addTask(TaskPo taskPo) throws Exception {
		checkTask(taskPo);
		if (taskPo.isCronExpression()) {
			addCronJob(taskPo);
		} else if (taskPo.isRepeatableExpression()) {
			addRepeatTask(taskPo);
		} else if (taskPo.isLimitExpression()) {
			List<TaskPo> executingTaskPos = executingTaskQueue.getTaskByTaskId(taskPo.getTaskId());
			List<TaskPo> executableTaskPos = executableTaskQueue.getDeadJob(0);
			if (executingTaskPos != null && !executingTaskPos.isEmpty() && executableTaskPos != null
					&& !executableTaskPos.isEmpty()) {
				return;
			}
			executableTaskQueue.add(taskPo);
		} else {
			executableTaskQueue.add(taskPo);
		}
	}

	/**
	 * 添加Cron 任务
	 */
	private static void addCronJob(TaskPo taskPo) throws DupEntryException {
		Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(taskPo.getCron());
		if (nextTriggerTime != null) {
			// 没有正在执行, 则添加
			List<TaskPo> taskPos = executingTaskQueue.getTaskByTaskId(taskPo.getTaskId());
			if (taskPos == null || taskPos.isEmpty()) {
				// 2. add to executable queue
				taskPo.setTriggerTime(nextTriggerTime.getTime());
				executableTaskQueue.add(taskPo);
			}
		}
	}

	/**
	 * 添加Repeat 任务
	 */
	private static void addRepeatTask(TaskPo taskPo) throws DupEntryException {
		// 没有正在执行, 则添加
		List<TaskPo> taskPos = executingTaskQueue.getTaskByTaskId(taskPo.getTaskId());
		if (taskPos == null || taskPos.isEmpty()) {
			// 2. add to executable queue
			executableTaskQueue.add(taskPo);
		}
	}

	public static void checkTask(TaskPo taskPo) throws Exception {
		if (RunningEnum.NOT_RUNNING.getCode() != taskPo.getIsRunning()) {
			throw new Exception("任务不能是运行中状态");
		}
		if (taskPo.getPoolId() == null) {
			throw new Exception("任务必须包含线程池id");
		}
		if (poolQueue.getPool(taskPo.getPoolId()) == null) {
			throw new Exception("任务线程池在线程池配置中不存在，请使用存在的线程池id");
		}

		if (taskPo.isCronExpression()) {
			if (CronExpressionUtils.isValidExpression(taskPo.getCron())) {
				throw new Exception("计划任务的cron配置有有误");
			}
			if (TaskExecType.SCHEDULETIME.getCode().equals(taskPo.getTaskExecType())) {
				throw new Exception("计划任务的执行方式配置有误");
			}
		}
		if (taskPo.isRepeatableExpression()) {
			if (taskPo.getRepeatCount() == null) {
				throw new Exception("重复执行任务的重复次数不能为空");
			}
			if (taskPo.getRepeatInterval() == null) {
				throw new Exception("重复执行任务的重复间隔不能为空");
			}
		}
	}

}
