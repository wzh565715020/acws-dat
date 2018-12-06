package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
@SPI(key = SpiExtensionKey.JOB_QUEUE, dftValue = "mysql")
public interface JobQueueFactory {

    CronJobQueue getCronJobQueue(Config config);

    RepeatJobQueue getRepeatJobQueue(Config config);

    ExecutableJobQueue getExecutableJobQueue(Config config);

    ExecutingJobQueue getExecutingJobQueue(Config config);

    JobFeedbackQueue getJobFeedbackQueue(Config config);

    NodeGroupStore getNodeGroupStore(Config config);

    SuspendJobQueue getSuspendJobQueue(Config config);

    PreLoader getPreLoader(AppContext appContext);
}

