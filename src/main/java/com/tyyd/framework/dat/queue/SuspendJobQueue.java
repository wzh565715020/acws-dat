package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.JobPo;
import com.tyyd.framework.dat.store.jdbc.exception.DupEntryException;

/**
 * 暂停队列
 *
 * @author Robert HG (254963746@qq.com) on 5/27/15.
 */
public interface SuspendJobQueue extends JobQueue{

    /**
     * 添加任务
     *
     * @throws DupEntryException
     */
    boolean add(JobPo jobPo);

    JobPo getJob(String jobId);

    /**
     * 移除Cron Job
     */
    boolean remove(String jobId);

}
