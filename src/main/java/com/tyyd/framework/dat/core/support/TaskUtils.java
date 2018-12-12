package com.tyyd.framework.dat.core.support;

import com.tyyd.framework.dat.queue.domain.TaskPo;

public class TaskUtils {

    public static long getRepeatNextTriggerTime(TaskPo jobPo) {
        long firstTriggerTime = Long.valueOf(jobPo.getTriggerTime());
        long now = SystemClock.now();
        long remainder = (now - firstTriggerTime) % jobPo.getRepeatInterval();
        if (remainder == 0) {
            return now;
        }
        return now + (jobPo.getRepeatInterval() - remainder);
    }

}
