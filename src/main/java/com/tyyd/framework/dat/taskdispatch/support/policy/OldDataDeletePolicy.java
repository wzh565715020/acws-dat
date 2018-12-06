package com.tyyd.framework.dat.taskdispatch.support.policy;

import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.JobFeedbackQueue;
import com.tyyd.framework.dat.queue.domain.JobFeedbackPo;
import com.tyyd.framework.dat.taskdispatch.support.OldDataHandler;

public class OldDataDeletePolicy implements OldDataHandler {

    private long expired = 30 * 24 * 60 * 60 * 1000L;        // 默认30 天

    public OldDataDeletePolicy() {
    }

    public OldDataDeletePolicy(long expired) {
        this.expired = expired;
    }

    public boolean handle(JobFeedbackQueue jobFeedbackQueue, JobFeedbackPo jobFeedbackPo, JobFeedbackPo po) {

        if (SystemClock.now() - jobFeedbackPo.getGmtCreated() > expired) {
            // delete
            jobFeedbackQueue.remove(po.getJobRunResult().getJobMeta().getJob().getTaskTrackerNodeGroup(), po.getId());
            return true;
        }

        return false;
    }
}
