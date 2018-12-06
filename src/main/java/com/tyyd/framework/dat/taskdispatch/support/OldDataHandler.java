package com.tyyd.framework.dat.taskdispatch.support;

import com.tyyd.framework.dat.queue.JobFeedbackQueue;
import com.tyyd.framework.dat.queue.domain.JobFeedbackPo;

/**
 * 老数据处理handler（像那种JobClient）
 *
 */
public interface OldDataHandler {

    boolean handle(JobFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po);

}
