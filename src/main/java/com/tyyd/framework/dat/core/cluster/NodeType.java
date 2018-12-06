package com.tyyd.framework.dat.core.cluster;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;

/**
 * @author Robert HG (254963746@qq.com) on 6/22/14.
 */
public enum NodeType {

    // job tracker
    JOB_TRACKER,
    // task tracker
    TASK_TRACKER,
    // client
    JOB_CLIENT,
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
