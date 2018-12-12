package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;

public interface PreLoader {

    public TaskPo take(String taskTrackerIdentity);
}
