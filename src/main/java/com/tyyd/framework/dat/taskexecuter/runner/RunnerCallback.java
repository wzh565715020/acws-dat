package com.tyyd.framework.dat.taskexecuter.runner;

import com.tyyd.framework.dat.core.domain.JobMeta;
import com.tyyd.framework.dat.taskexecuter.domain.Response;

public interface RunnerCallback {

    /**
     * 执行完成, 可能是成功, 也可能是失败
     * @param response
     * @return 如果有新的任务, 那么返回新的任务过来
     */
    public JobMeta runComplete(Response response);

}
