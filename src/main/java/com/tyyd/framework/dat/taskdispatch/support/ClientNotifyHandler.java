package com.tyyd.framework.dat.taskdispatch.support;


import java.util.List;

import com.tyyd.framework.dat.core.domain.JobRunResult;

public interface ClientNotifyHandler<T extends JobRunResult> {

    /**
     * 通知成功的处理
     */
    public void handleSuccess(List<T> jobResults);

    /**
     * 通知失败的处理
     */
    public void handleFailed(List<T> jobResults);

}
