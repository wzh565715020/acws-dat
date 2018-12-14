package com.tyyd.framework.dat.core.support;

import com.tyyd.framework.dat.core.exception.CronException;
import com.tyyd.framework.dat.core.support.CronExpression;

import java.text.ParseException;
import java.util.Date;

public class CronExpressionUtils {

    private CronExpressionUtils() {
    }

    public static Date getNextTriggerTime(String cronExpression) {
        try {
            CronExpression cron = new CronExpression(cronExpression);
            return cron.getTimeAfter(new Date());
        } catch (ParseException e) {
            throw new CronException(e);
        }
    }

    public static boolean isValidExpression(String cronExpression) {
        return CronExpression.isValidExpression(cronExpression);
    }

}
