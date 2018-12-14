package com.tyyd.framework.dat.management.monitor.cmd;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tyyd.framework.dat.core.commons.utils.BeanUtils;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.domain.monitor.JvmMData;
import com.tyyd.framework.dat.core.domain.monitor.MData;
import com.tyyd.framework.dat.core.domain.monitor.MNode;
import com.tyyd.framework.dat.core.exception.LtsRuntimeException;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.management.monitor.MonitorAppContext;
import com.tyyd.framework.dat.management.monitor.access.domain.JVMGCDataPo;
import com.tyyd.framework.dat.management.monitor.access.domain.JVMMemoryDataPo;
import com.tyyd.framework.dat.management.monitor.access.domain.JVMThreadDataPo;
import com.tyyd.framework.dat.management.monitor.access.domain.JobClientMDataPo;
import com.tyyd.framework.dat.management.monitor.access.domain.JobTrackerMDataPo;
import com.tyyd.framework.dat.management.monitor.access.domain.MDataPo;
import com.tyyd.framework.dat.management.monitor.access.domain.TaskTrackerMDataPo;


public class MDataSrv {

    private MonitorAppContext appContext;

    public MDataSrv(MonitorAppContext appContext) {
        this.appContext = appContext;
    }

    public void addMDatas(MNode mNode, List<MData> mDatas) {
        if (CollectionUtils.isEmpty(mDatas)) {
            return;
        }

        switch (mNode.getNodeType()) {
            case TASK_CLIENT:
                addJobClientMData(mNode, mDatas);
                break;
            case TASK_DISPATCH:
                addJobTrackerMData(mNode, mDatas);
                break;
            case TASK_EXECUTER:
                addTaskTrackerMData(mNode, mDatas);
                break;
            default:
                throw new LtsRuntimeException("Unsupport nodeType:" + mNode.getNodeType());
        }
    }

    private void addJobClientMData(MNode mNode, List<MData> mDatas) {
        List<JobClientMDataPo> pos = new ArrayList<JobClientMDataPo>(mDatas.size());
        for (MData mData : mDatas) {
            JobClientMDataPo po = new JobClientMDataPo();
            BeanUtils.copyProperties(po, mData);
            po.setNodeType(mNode.getNodeType());
            po.setIdentity(mNode.getIdentity());
            po.setGmtCreated(SystemClock.now());
            pos.add(po);
        }

        appContext.getJobClientMAccess().insert(pos);

        // 添加jvm监控数据
        addJvmMData(mNode, mDatas);
    }

    private void addJobTrackerMData(MNode mNode, List<MData> mDatas) {

        List<JobTrackerMDataPo> pos = new ArrayList<JobTrackerMDataPo>(mDatas.size());
        for (MData mData : mDatas) {
            JobTrackerMDataPo po = new JobTrackerMDataPo();
            BeanUtils.copyProperties(po, mData);
            po.setNodeType(mNode.getNodeType());
            po.setIdentity(mNode.getIdentity());
            po.setGmtCreated(SystemClock.now());
            pos.add(po);
        }

        appContext.getJobTrackerMAccess().insert(pos);

        // 添加jvm监控数据
        addJvmMData(mNode, mDatas);
    }

    private void addTaskTrackerMData(MNode mNode, List<MData> mDatas) {

        List<TaskTrackerMDataPo> pos = new ArrayList<TaskTrackerMDataPo>(mDatas.size());
        for (MData mData : mDatas) {
            TaskTrackerMDataPo po = new TaskTrackerMDataPo();
            BeanUtils.copyProperties(po, mData);
            po.setNodeType(mNode.getNodeType());
            po.setIdentity(mNode.getIdentity());
            po.setGmtCreated(SystemClock.now());
            pos.add(po);
        }
        appContext.getTaskTrackerMAccess().insert(pos);

        // 添加jvm监控数据
        addJvmMData(mNode, mDatas);
    }

    public void addJvmMData(MNode mNode, List<MData> mDatas) {

        int size = mDatas.size();
        List<JVMGCDataPo> jvmGCDataPos = new ArrayList<JVMGCDataPo>(size);
        List<JVMMemoryDataPo> jvmMemoryDataPos = new ArrayList<JVMMemoryDataPo>(size);
        List<JVMThreadDataPo> jvmThreadDataPos = new ArrayList<JVMThreadDataPo>(size);

        for (MData mData : mDatas) {

            JvmMData JVMMData = mData.getJvmMData();
            Long timestamp = mData.getTimestamp();
            // gc
            JVMGCDataPo jvmgcDataPo = getDataPo(JVMMData.getGcMap(), JVMGCDataPo.class, mNode, timestamp);
            jvmGCDataPos.add(jvmgcDataPo);
            // memory
            JVMMemoryDataPo jvmMemoryDataPo = getDataPo(JVMMData.getMemoryMap(), JVMMemoryDataPo.class, mNode, timestamp);
            jvmMemoryDataPos.add(jvmMemoryDataPo);
            // thread
            JVMThreadDataPo jvmThreadDataPo = getDataPo(JVMMData.getThreadMap(), JVMThreadDataPo.class, mNode, timestamp);
            jvmThreadDataPos.add(jvmThreadDataPo);
        }

        appContext.getJvmGCAccess().insert(jvmGCDataPos);
        appContext.getJvmMemoryAccess().insert(jvmMemoryDataPos);
        appContext.getJvmThreadAccess().insert(jvmThreadDataPos);
    }


    private static final Map<String, Method> CACHED_METHOD_MAP = new ConcurrentHashMap<String, Method>();

    static {
        cacheMethod(JVMGCDataPo.class);
        cacheMethod(JVMMemoryDataPo.class);
        cacheMethod(JVMThreadDataPo.class);
    }

    private static void cacheMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set")) {
                CACHED_METHOD_MAP.put(clazz.getSimpleName() + "_" + method.getName().substring(3), method);
            }
        }
    }

    /**
     * 根据Map得到 持久化对象
     */
    private <T extends MDataPo> T getDataPo(Map<String, Object> dataMap, Class<T> clazz,
                                            MNode mNode, Long timestamp) {
        try {
            T data = clazz.newInstance();
            if (CollectionUtils.isNotEmpty(dataMap)) {
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    Method method = CACHED_METHOD_MAP.get(clazz.getSimpleName() + "_" + entry.getKey());
                    if (method != null) {
                        String string = String.valueOf(entry.getValue());
                        Object value = entry.getValue();
                        Class<?> parameterType = method.getParameterTypes()[0];
                        if (parameterType == Long.class || parameterType == long.class) {
                            value = Long.valueOf(string);
                        } else if (parameterType == Integer.class || parameterType == int.class) {
                            value = Integer.valueOf(string);
                        } else if (parameterType == Float.class || parameterType == float.class) {
                            value = Float.valueOf(string);
                        } else if (parameterType == Double.class || parameterType == double.class) {
                            value = Double.valueOf(string);
                        } else if (parameterType == Short.class || parameterType == short.class) {
                            value = Short.valueOf(string);
                        } else if (parameterType == Boolean.class || parameterType == boolean.class) {
                            value = Boolean.valueOf(string);
                        } else if (parameterType == String.class) {
                            value = string;
                        }
                        // TODO others
                        method.invoke(data, value);
                    }
                }
            }

            data.setNodeType(mNode.getNodeType());
            data.setIdentity(mNode.getIdentity());
            data.setGmtCreated(SystemClock.now());
            data.setTimestamp(timestamp);

            return data;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
