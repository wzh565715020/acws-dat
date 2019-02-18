package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.queue.ExecutableTaskQueue;
import com.tyyd.framework.dat.queue.ExecutedTaskQueue;
import com.tyyd.framework.dat.queue.ExecutingTaskQueue;
import com.tyyd.framework.dat.queue.PoolQueue;
import com.tyyd.framework.dat.queue.PreLoader;
import com.tyyd.framework.dat.queue.TaskQueue;
import com.tyyd.framework.dat.queue.TaskQueueFactory;

public class MysqlTaskQueueFactory implements TaskQueueFactory {

    @Override
    public TaskQueue getTaskQueue(Config config) {
        return new MysqlTaskQueue();
    }

    @Override
    public ExecutableTaskQueue getExecutableJobQueue(Config config) {
        return new MysqlExecutableTaskQueue();
    }

    @Override
    public ExecutingTaskQueue getExecutingJobQueue(Config config) {
        return new MysqlExecutingTaskQueue();
    }


    @Override
    public PreLoader getPreLoader(AppContext appContext,String poolId) {
        return new MysqlPreLoader(appContext,poolId);
    }
    @Override
    public PoolQueue getPoolQueue(Config config) {
        return new MysqlPoolQueue();
    }

	@Override
	public ExecutedTaskQueue getExecutedJobQueue(Config config) {
		return new MysqlExecutedTaskQueue();
	}
}
