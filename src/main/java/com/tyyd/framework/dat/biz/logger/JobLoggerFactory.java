package com.tyyd.framework.dat.biz.logger;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
@SPI(key = SpiExtensionKey.TASK_LOGGER, dftValue = "mysql")
public interface JobLoggerFactory {

    JobLogger getJobLogger(Config config);

}
