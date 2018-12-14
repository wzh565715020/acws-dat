package com.tyyd.framework.dat.jvmmonitor.mbean;

/**
 * @author   on 9/15/15.
 */
public interface JVMGCMBean {

    long getYoungGCCollectionCount();

    long getYoungGCCollectionTime();

    long getFullGCCollectionCount();

    long getFullGCCollectionTime();

    // 下面的数字是做过差计算的,启动后的第二次开始才能做差值
    long getSpanYoungGCCollectionCount();

    long getSpanYoungGCCollectionTime();

    long getSpanFullGCCollectionCount();

    long getSpanFullGCCollectionTime();

}
