package com.tyyd.framework.dat.taskdispatch.support;

import com.tyyd.framework.dat.queue.TaskFeedbackQueue;
import com.tyyd.framework.dat.queue.domain.JobFeedbackPo;

/**
 * 老数据处理handler（像那种JobClient）
 *
 */
public interface OldDataHandler {

    boolean handle(TaskFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po);

}
