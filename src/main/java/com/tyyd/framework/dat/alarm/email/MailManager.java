package com.tyyd.framework.dat.alarm.email;

public interface MailManager {
    void send(String to, String title, String message) throws Exception;
}

