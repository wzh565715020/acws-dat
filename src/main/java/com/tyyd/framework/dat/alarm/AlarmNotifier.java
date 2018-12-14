package com.tyyd.framework.dat.alarm;

/**
 * @author    on 2/17/16.
 */
public interface AlarmNotifier<T extends AlarmMessage> {

    /**
     * 告警发送通知
     */
    void notice(T message);

}
