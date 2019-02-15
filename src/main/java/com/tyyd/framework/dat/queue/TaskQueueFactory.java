package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

/**
 * @author   on 3/12/16.
 */
@SPI(key = SpiExtensionKey.TASK_QUEUE, dftValue = "mysql")
public interface TaskQueueFactory {

    TaskQueue getTaskQueue(Config config);

    ExecutableTaskQueue getExecutableJobQueue(Config config);

    ExecutingTaskQueue getExecutingJobQueue(Config config);
    
    PreLoader getPreLoader(AppContext appContext,String poolId);

    PoolQueue getPoolQueue(Config config);

	ExecutedTaskQueue getExecutedJobQueue(Config config);
}

