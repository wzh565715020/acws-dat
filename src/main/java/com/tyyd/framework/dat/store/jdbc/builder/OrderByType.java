package com.tyyd.framework.dat.store.jdbc.builder;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;

public enum OrderByType {
    DESC, ASC;

    public static OrderByType convert(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        return OrderByType.valueOf(value);
    }

}
