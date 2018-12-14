package com.tyyd.framework.dat.jvmmonitor.mbean;

import java.math.BigDecimal;

/**
 * @author   on 9/15/15.
 */
public interface JVMThreadMBean {

    int getDaemonThreadCount();

    int getThreadCount();

    long getTotalStartedThreadCount();

    int getDeadLockedThreadCount();

    BigDecimal getProcessCpuTimeRate();
}
