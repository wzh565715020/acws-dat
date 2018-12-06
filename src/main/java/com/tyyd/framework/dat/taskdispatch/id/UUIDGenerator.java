package com.tyyd.framework.dat.taskdispatch.id;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.queue.domain.JobPo;

public class UUIDGenerator implements IdGenerator{
    @Override
    public String generate(JobPo jobPo) {
        return StringUtils.generateUUID();
    }
}
