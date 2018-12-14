package com.tyyd.framework.dat.biz.logger;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

/**
 * @author   on 5/19/15.
 */
@SPI(key = SpiExtensionKey.TASK_LOGGER, dftValue = "mysql")
public interface JobLoggerFactory {

    TaskLogger getJobLogger(Config config);

}
