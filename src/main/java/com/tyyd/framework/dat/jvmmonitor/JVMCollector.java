package com.tyyd.framework.dat.jvmmonitor;

import com.tyyd.framework.dat.core.domain.monitor.JvmMData;
import com.tyyd.framework.dat.jvmmonitor.mbean.JVMGCMBean;
import com.tyyd.framework.dat.jvmmonitor.mbean.JVMInfoMBean;
import com.tyyd.framework.dat.jvmmonitor.mbean.JVMMemoryMBean;
import com.tyyd.framework.dat.jvmmonitor.mbean.JVMThreadMBean;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author   on 9/15/15.
 */
public class JVMCollector {

    /**
     * 收集信息
     */
    public static JvmMData collect() {

        JvmMData JVMMData = new JvmMData();
        // memory
        Map<String, Object> memoryMap = JVMMonitor.getAttribute(JVMConstants.JMX_JVM_MEMORY_NAME,
                getAttributeList(JVMMemoryMBean.class));
        JVMMData.setMemoryMap(memoryMap);
        // gc
        Map<String, Object> gcMap = JVMMonitor.getAttribute(JVMConstants.JMX_JVM_GC_NAME,
                getAttributeList(JVMGCMBean.class));
        JVMMData.setGcMap(gcMap);

        // thread
        Map<String, Object> threadMap = JVMMonitor.getAttribute(JVMConstants.JMX_JVM_THREAD_NAME,
                getAttributeList(JVMThreadMBean.class));
        JVMMData.setThreadMap(threadMap);

        return JVMMData;
    }

    private static List<String> getAttributeList(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        List<String> attributeList = new ArrayList<String>(methods.length);
        for (Method method : methods) {
            // 去掉 get 前缀
            attributeList.add(method.getName().substring(3));
        }
        return attributeList;
    }

    public static Map<String, Object> getJVMInfo() {
        return JVMMonitor.getAttribute(JVMConstants.JMX_JVM_INFO_NAME,
                getAttributeList(JVMInfoMBean.class));
    }

}
