package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.queue.*;

public class MysqlTaskQueueFactory implements TaskQueueFactory {

    @Override
    public TaskQueue getTaskQueue(Config config) {
        return new MysqlTaskQueue(config);
    }

    @Override
    public ExecutableTaskQueue getExecutableJobQueue(Config config) {
        return new MysqlExecutableTaskQueue(config);
    }

    @Override
    public ExecutingTaskQueue getExecutingJobQueue(Config config) {
        return new MysqlExecutingJobQueue(config);
    }


    @Override
    public PreLoader getPreLoader(AppContext appContext) {
        return new MysqlPreLoader(appContext);
    }
}
