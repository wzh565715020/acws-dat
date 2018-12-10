package com.tyyd.framework.dat.core.support;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.queue.domain.TaskPo;

/**
 * @author Robert HG (254963746@qq.com) on 3/26/16.
 */
public class JobUtils {

    public static long getRepeatNextTriggerTime(TaskPo jobPo) {
        long firstTriggerTime = Long.valueOf(jobPo.getInternalExtParam(Constants.QUARTZ_FIRST_FIRE_TIME));
        long now = SystemClock.now();
        long remainder = (now - firstTriggerTime) % jobPo.getRepeatInterval();
        if (remainder == 0) {
            return now;
        }
        return now + (jobPo.getRepeatInterval() - remainder);
    }

}
