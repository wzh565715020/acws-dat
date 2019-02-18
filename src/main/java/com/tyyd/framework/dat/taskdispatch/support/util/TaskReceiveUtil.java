package com.tyyd.framework.dat.taskdispatch.support.util;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.MysqlExecutableTaskQueue;
import com.tyyd.framework.dat.queue.mysql.MysqlExecutingTaskQueue;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.id.UUIDGenerator;

/**
 *         任务处理器
 */
public class TaskReceiveUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskReceiveUtil.class);

    private static IdGenerator idGenerator = new UUIDGenerator();
    
    private static ExecutableTaskQueue executableTaskQueue = new MysqlExecutableTaskQueue();
    
    private static ExecutingTaskQueue executingTaskQueue = new MysqlExecutingTaskQueue();
    
    public static void addToQueue(Task task) {

        TaskPo taskPo = null;
        try {
            taskPo = TaskDomainConverter.convert(task);
            if (taskPo == null) {
                LOGGER.warn("task can not be null。{}", task);
                return;
            }
            // 设置 id
            taskPo.setId(idGenerator.generate(taskPo));

            // 添加任务
            addJob(taskPo);

        } catch (DupEntryException e) {
            // 已经存在
        } finally {
        }
    }

    /**
     * 添加任务
     */
    public static void addJob(TaskPo taskPo) throws DupEntryException {
        if (taskPo.isCron()) {
            addCronJob(taskPo);
        } else if (taskPo.isRepeatable()) {
            addRepeatTask(taskPo);
        }else {
        	List<TaskPo> executingTaskPos = executingTaskQueue.getTaskByTaskId(taskPo.getTaskId());
        	List<TaskPo> executableTaskPos = executableTaskQueue.getDeadJob(0);
        	if (executingTaskPos != null && !executingTaskPos.isEmpty() && executableTaskPos != null && !executableTaskPos.isEmpty()) {
				return;
			}
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
            if (executableTaskQueue.getTask(taskPo.getTaskId()) == null) {
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
        if (executableTaskQueue.getTask(taskPo.getTaskId()) == null) {
            // 2. add to executable queue
        	executableTaskQueue.add(taskPo);
        }
    }

}
