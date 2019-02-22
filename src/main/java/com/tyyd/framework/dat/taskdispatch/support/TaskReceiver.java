package com.tyyd.framework.dat.taskdispatch.support;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.exception.TaskReceiveException;
import com.tyyd.framework.dat.core.protocol.command.TaskSubmitRequest;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;
import com.tyyd.framework.dat.taskdispatch.support.util.TaskReceiveUtil;

/**
 *         任务处理器
 */
public class TaskReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskReceiver.class);

    private TaskDispatcherAppContext appContext;
    private IdGenerator idGenerator;
    private TaskDispatcherMStatReporter stat;
    protected AtomicBoolean started = new AtomicBoolean(false);
    public TaskReceiver(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();
        this.idGenerator = ServiceLoader.load(IdGenerator.class, appContext.getConfig());
    }

    public void receive(TaskSubmitRequest request) throws TaskReceiveException {

        List<Task> tasks = request.getTasks();
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        TaskReceiveException exception = null;
        for (Task task : tasks) {
            try {
                addToQueue(task, request);
            } catch (Exception t) {
                if (exception == null) {
                    exception = new TaskReceiveException(t);
                }
                exception.addJob(task);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private TaskPo addToQueue(Task task, TaskSubmitRequest request) throws Exception {

        TaskPo taskPo = null;
        boolean success = false;
        try {
            taskPo = TaskDomainConverter.convert(task);
            if (taskPo == null) {
                LOGGER.warn("task can not be null。{}", task);
                return null;
            }
            if (StringUtils.isEmpty(taskPo.getSubmitNode())) {
                taskPo.setSubmitNode(request.getIdentity());
            }
            // 设置 jobId
            taskPo.setId(idGenerator.generate());
            // 添加任务
            addJob(taskPo);
            success = true;

       } finally {
            if (success) {
                stat.incReceiveJobNum();
            }
        }
        return taskPo;
    }

    /**
     * 添加任务
     */
    public void addJob(TaskPo taskPo) throws Exception {
    	TaskReceiveUtil.checkTask(taskPo);
        if (taskPo.isCronExpression()) {
            addCronTask(taskPo);
        } else if (taskPo.isRepeatableExpression()) {
            addRepeatTask(taskPo);
        }else {
        	 appContext.getExecutableTaskQueue().add(taskPo);
        }
    }
    /**
     * 添加Cron 任务
     */
    private void addCronTask(TaskPo taskPo) throws DupEntryException {
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(taskPo.getCron());
        if (nextTriggerTime != null) {
            // 没有正在执行, 则添加
            if (appContext.getExecutableTaskQueue().getTask(taskPo.getTaskId()) == null) {
                // 2. add to executable queue
                taskPo.setTriggerTime(nextTriggerTime.getTime());
                appContext.getExecutableTaskQueue().add(taskPo);
            }
        }
    }
    /**
     * 添加Repeat 任务
     */
    private void addRepeatTask(TaskPo taskPo) throws DupEntryException {
        // 没有正在执行, 则添加
        if (appContext.getExecutableTaskQueue().getTask(taskPo.getTaskId()) == null) {
            // 2. add to executable queue
            appContext.getExecutableTaskQueue().add(taskPo);
        }
    }

}
