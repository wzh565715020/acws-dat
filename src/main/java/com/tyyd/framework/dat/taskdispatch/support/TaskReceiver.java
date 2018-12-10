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
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.core.exception.JobReceiveException;
import com.tyyd.framework.dat.core.protocol.command.JobSubmitRequest;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.core.support.CronExpressionUtils;
import com.tyyd.framework.dat.core.support.JobDomainConverter;
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
    public void receive(JobSubmitRequest request) throws JobReceiveException {

        List<Job> jobs = request.getJobs();
        if (CollectionUtils.isEmpty(jobs)) {
            return;
        }
        JobReceiveException exception = null;
        for (Job job : jobs) {
            try {
                addToQueue(job, request);
            } catch (Exception t) {
                if (exception == null) {
                    exception = new JobReceiveException(t);
                }
                exception.addJob(job);
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    private TaskPo addToQueue(Job job, JobSubmitRequest request) {

        TaskPo jobPo = null;
        boolean success = false;
        BizLogCode code = null;
        try {
            jobPo = JobDomainConverter.convert(job);
            if (jobPo == null) {
                LOGGER.warn("Job can not be null。{}", job);
                return null;
            }
            if (StringUtils.isEmpty(jobPo.getSubmitNodeGroup())) {
                jobPo.setSubmitNodeGroup(request.getNodeGroup());
            }
            // 设置 jobId
            jobPo.setJobId(idGenerator.generate(jobPo));

            // 添加任务
            addJob(job, jobPo);

            success = true;
            code = BizLogCode.SUCCESS;

        } catch (DupEntryException e) {
            // 已经存在
            if (job.isReplaceOnExist()) {
                Assert.notNull(jobPo);
                success = replaceOnExist(job, jobPo);
                code = success ? BizLogCode.DUP_REPLACE : BizLogCode.DUP_FAILED;
            } else {
                code = BizLogCode.DUP_IGNORE;
                LOGGER.info("Job already exist. nodeGroup={}, {}", request.getNodeGroup(), job);
            }
        } finally {
            if (success) {
                stat.incReceiveJobNum();
            }
        }

        // 记录日志
        jobBizLog(jobPo, code);

        return jobPo;
    }

    /**
     * 添加任务
     */
    private void addJob(Job job, TaskPo jobPo) throws DupEntryException {
        if (job.isCron()) {
            addCronJob(jobPo);
        } else if (job.isRepeatable()) {
            addRepeatJob(jobPo);
        } else {
            boolean needAdd2ExecutableJobQueue = true;
            String ignoreAddOnExecuting = CollectionUtils.getValue(jobPo.getInternalExtParams(), "__LTS_ignoreAddOnExecuting");
            if (ignoreAddOnExecuting != null && "true".equals(ignoreAddOnExecuting)) {
                if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) != null) {
                    needAdd2ExecutableJobQueue = false;
                }
            }
            if (needAdd2ExecutableJobQueue) {
                appContext.getExecutableJobQueue().add(jobPo);
            }
        }
        LOGGER.info("Receive Job success. {}", job);
    }

    /**
     * 更新任务
     **/
    private boolean replaceOnExist(Job job, TaskPo jobPo) {

        // 得到老的jobId
        TaskPo oldJobPo;
        if (job.isCron()) {
            oldJobPo = appContext.getCronJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
        } else if (job.isRepeatable()) {
            oldJobPo = appContext.getRepeatJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
        } else {
            oldJobPo = appContext.getExecutableJobQueue().getJob(job.getTaskTrackerNodeGroup(), job.getTaskId());
        }
        if (oldJobPo != null) {
            String jobId = oldJobPo.getJobId();
            // 1. 删除任务
            appContext.getExecutableJobQueue().remove(job.getTaskTrackerNodeGroup(), jobId);
            if (job.isCron()) {
                appContext.getCronJobQueue().remove(jobId);
            } else if (job.isRepeatable()) {
                appContext.getRepeatJobQueue().remove(jobId);
            }
            jobPo.setJobId(jobId);
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
        Date nextTriggerTime = CronExpressionUtils.getNextTriggerTime(jobPo.getCronExpression());
        if (nextTriggerTime != null) {
            // 1.add to cron job queue
            appContext.getCronJobQueue().add(jobPo);

            // 没有正在执行, 则添加
            if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) == null) {
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
        appContext.getRepeatJobQueue().add(jobPo);

        // 没有正在执行, 则添加
        if (appContext.getExecutingJobQueue().getJob(jobPo.getTaskTrackerNodeGroup(), jobPo.getTaskId()) == null) {
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
            JobLogPo jobLogPo = JobDomainConverter.convertJobLog(jobPo);
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
