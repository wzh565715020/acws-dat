package com.tyyd.framework.dat.taskdispatch.id;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.queue.domain.TaskPo;

public class UUIDGenerator implements IdGenerator{
    @Override
    public String generate(TaskPo jobPo) {
        return StringUtils.generateUUID();
    }
}
