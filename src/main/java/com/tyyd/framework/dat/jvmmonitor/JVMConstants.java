package com.tyyd.framework.dat.jvmmonitor;

/**
 * @author Robert HG (254963746@qq.com) on 9/15/15.
 */
public interface JVMConstants {

    String JMX_JVM_INFO_NAME = "com.tyyd.framework.dat.jvmmonitor:type=JVMInfo";
    String JMX_JVM_MEMORY_NAME = "com.tyyd.framework.dat.jvmmonitor:type=JVMMemory";
    String JMX_JVM_GC_NAME = "com.tyyd.framework.dat.jvmmonitor:type=JVMGC";
    String JMX_JVM_THREAD_NAME = "com.tyyd.framework.dat.jvmmonitor:type=JVMThread";
}
