package com.tyyd.framework.dat.alarm.email;

import com.tyyd.framework.dat.alarm.AlarmMessage;

/**
 * @author  on 2/17/16.
 */
public class EmailAlarmMessage extends AlarmMessage {

    private String to;

    private String title;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
