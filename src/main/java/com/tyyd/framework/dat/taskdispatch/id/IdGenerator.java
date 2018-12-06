package com.tyyd.framework.dat.taskdispatch.id;

import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.queue.domain.JobPo;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

@SPI(key = SpiExtensionKey.JOB_ID_GENERATOR, dftValue = "md5")
public interface IdGenerator {

    /**
     * 生成ID
     */
    public String generate(JobPo jobPo);

}
