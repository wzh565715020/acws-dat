package com.tyyd.framework.dat.store.jdbc.dbutils;

import java.util.Date;

public class JdbcTypeUtils {

    public static Long toTimestamp(Date date) {

        if (date == null) {
            return null;
        }
        return date.getTime();
    }

    public static Date toDate(Long timestamp){
        if(timestamp == null){
            return null;
        }
        return new Date(timestamp);
    }
}
