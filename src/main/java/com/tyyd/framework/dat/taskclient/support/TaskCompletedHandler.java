package com.tyyd.framework.dat.taskclient.support;


import java.util.List;

import com.tyyd.framework.dat.core.domain.JobResult;

public interface TaskCompletedHandler {

    /**
     * 处理返回结果
     */
    public void onComplete(List<JobResult> jobResults);
}
