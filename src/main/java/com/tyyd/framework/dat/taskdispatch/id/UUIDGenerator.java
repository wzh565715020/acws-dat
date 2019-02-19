package com.tyyd.framework.dat.taskdispatch.id;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;

public class UUIDGenerator implements IdGenerator{
    @Override
    public String generate() {
        return StringUtils.generateUUID();
    }
}
