package com.tyyd.framework.dat.core.cluster;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;

public enum NodeType {

    // TASK_DISPATCH
    TASK_DISPATCH,
    // TASK_EXECUTER
    TASK_EXECUTER,
    // client
    TASK_CLIENT,
    // monitor
    MONITOR,

    BACKEND;

    public static NodeType convert(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return NodeType.valueOf(value);
    }
}
