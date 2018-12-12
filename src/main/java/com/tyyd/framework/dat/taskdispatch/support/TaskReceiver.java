package com.tyyd.framework.dat.taskdispatch.support;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.commons.utils.Assert;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.exception.JobReceiveException;
import com.tyyd.framework.dat.core.protocol.command.TaskSubmitRequest;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.id.IdGenerator;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;

/**
 *         任务处理器
 */
public class TaskReceiver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskReceiver.class);

    private TaskDispatcherAppContext appContext;
    private IdGenerator idGenerator;
    private TaskDispatcherMStatReporter stat;

    public TaskReceiver(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();
        this.idGenerator = ServiceLoader.load(IdGenerator.class, appContext.getConfig());
    }

    /**
     * jobTracker 接受任务
     */
    public void receive(TaskSubmitRequest request) throws JobReceiveException {

        List<Task> tasks = request.getJobs();
        if (CollectionUtils.isEmpty(tasks)) {
            return;
        }
        JobReceiveException exception = null;
        for (Task task : tasks) {
            try {
                addToQueue(task, request);
            } catch (Exception t) {
                if (exception == null) {
                    exception = new JobReceiveException(t);
                }
                exception.addJob(task);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private TaskPo addToQueue(Task task, TaskSubmitRequest request) {

        TaskPo taskPo = null;
        boolean success = false;
        BizLogCode code = null;
        try {
            taskPo = TaskDomainConverter.convert(task);
            if (taskPo == null) {
                LOGGER.warn("Job can not be null。{}", task);
                return null;
            }
            if (StringUtils.isEmpty(taskPo.getSubmitNode())) {
                taskPo.setSubmitNode(request.getNodeGroup());
            }
            // 设置 jobId
            taskPo.setTaskId(idGenerator.generate(taskPo));

            // 添加任务
            addJob(task, taskPo);

            success = true;
            code = BizLogCode.SUCCESS;

        } catch (DupEntryException e) {
            // 已经存在
                LOGGER.info("Job already exist. nodeGroup={}, {}", request.getNodeGroup(), task);
        } finally {
            if (success) {
                stat.incReceiveJobNum();
            }
        }

        // 记录日志
        jobBizLog(taskPo, code);

        return taskPo;
    }

    /**
     * 添加任务
     */
    private void addJob(Task job, TaskPo taskPo) throws DupEntryException {
        if (job.isCron()) {
            addCronJob(taskPo);
        } else if (job.isRepeatable()) {
            addRepeatJob(taskPo);
        } else {
            appContext.getExecutableJobQueue().add(taskPo);
        }
        LOGGER.info("Receive Job success. {}", job);
    }

    /**
     * 更新任务
     **/
    private boolean replaceOnExist(Task job, TaskPo jobPo) {

        // 得到老的jobId
        TaskPo oldJobPo;
        if (job.isCron()) {
            oldJobPo = appContext.getTaskQueue().getTask(job.getTaskId());
        } else if (job.isRepeatable()) {
            oldJobPo = appContext.getTaskQueue().getTask(job.getTaskId());
        } else {
            oldJobPo = appContext.getExecutableJobQueue().getTask(job.getTaskId());
        }
        if (oldJobPo != null) {
            String jobId = oldJobPo.getTaskId();
            // 1. 删除任务
            appContext.getExecutableJobQueue().remove(jobId);
            if (job.isCron()) {
                appContext.getTaskQueue().remove(jobId);
            } else if (job.isRepeatable()) {
                appContext.getTaskQueue().remove(jobId);
            }
            jobPo.setTaskId(jobId);
        }

        // 2. 重新添加任务
        try {
            addJob(job, jobPo);
        } catch (DupEntryException e) {
            // 一般不会走到这里
            LOGGER.warn("Job already exist twice. {}", job);
            return false;
        }
        return true;
    }

    /**
     * 添加Cron 任务
     */
    private void addCronJob(TaskPo jobPo) throws DupEntryException {
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCron());
        if (nextTriggerTime != null) {
            // 1.add to cron job queue
            appContext.getTaskQueue().add(jobPo);

            // 没有正在执行, 则添加
            if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskId()) == null) {
                // 2. add to executable queue
                jobPo.setTriggerTime(nextTriggerTime.getTime());
                appContext.getExecutableJobQueue().add(jobPo);
            }
        }
    }

    /**
     * 添加Repeat 任务
     */
    private void addRepeatJob(TaskPo jobPo) throws DupEntryException {
        // 1.add to repeat job queue
        appContext.getTaskQueue().add(jobPo);

        // 没有正在执行, 则添加
        if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskId()) == null) {
            // 2. add to executable queue
            appContext.getExecutableJobQueue().add(jobPo);
        }
    }

    /**
     * 记录任务日志
     */
    private void jobBizLog(TaskPo jobPo, BizLogCode code) {
        if (jobPo == null) {
            return;
        }

        try {
            // 记录日志
            JobLogPo jobLogPo = TaskDomainConverter.convertJobLog(jobPo);
            jobLogPo.setSuccess(true);
            jobLogPo.setLogType(LogType.RECEIVE);
            jobLogPo.setLogTime(SystemClock.now());

            switch (code) {
                case SUCCESS:
                    jobLogPo.setLevel(Level.INFO);
                    jobLogPo.setMsg("Receive Success");
                    break;
                case DUP_IGNORE:
                    jobLogPo.setLevel(Level.WARN);
                    jobLogPo.setMsg("Already Exist And Ignored");
                    break;
                case DUP_FAILED:
                    jobLogPo.setLevel(Level.ERROR);
                    jobLogPo.setMsg("Already Exist And Update Failed");
                    break;
                case DUP_REPLACE:
                    jobLogPo.setLevel(Level.INFO);
                    jobLogPo.setMsg("Already Exist And Update Success");
                    break;
            }

            appContext.getJobLogger().log(jobLogPo);
        } catch (Throwable t) {     // 日志记录失败不影响正常运行
            LOGGER.error("Receive Job Log error ", t);
        }
    }

    private enum BizLogCode {
        DUP_IGNORE,     // 添加重复并忽略
        DUP_REPLACE,    // 添加时重复并覆盖更新
        DUP_FAILED,     // 添加时重复再次添加失败
        SUCCESS,     // 添加成功
    }

}
