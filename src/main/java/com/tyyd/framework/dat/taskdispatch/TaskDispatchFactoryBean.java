package com.tyyd.framework.dat.taskdispatch;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.tyyd.framework.dat.core.commons.utils.Assert;
import com.tyyd.framework.dat.core.listener.MasterChangeListener;

import java.util.Map;
import java.util.Properties;

/**
 * TaskDispatch Spring Bean 工厂类
 */
public class TaskDispatchFactoryBean implements FactoryBean<TaskDispatcher>,
        InitializingBean, DisposableBean {

    private TaskDispatcher taskDispatcher;
    
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
  
    /**
     * 监听端口
     */
    private Integer listenPort;
    
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();

    @Override
    public TaskDispatcher getObject() throws Exception {
        return taskDispatcher;
    }

    @Override
    public Class<?> getObjectType() {
        return TaskDispatcher.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void checkProperties() {
        Assert.hasText(clusterName, "clusterName must have value.");
        Assert.LessThanMaxLength(clusterName,28,"");
        Assert.hasText(registryAddress, "registryAddress must have value.");
        Assert.notNull(listenPort,"listenPort must have value.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        taskDispatcher = new TaskDispatcher();

        taskDispatcher.setClusterName(clusterName);
        taskDispatcher.setRegistryAddress(registryAddress);
        if (listenPort != null) {
        	taskDispatcher.setListenPort(listenPort);
        }
        // 设置config
        for (Map.Entry<Object, Object> entry : configs.entrySet()) {
            taskDispatcher.addConfig(entry.getKey().toString(), entry.getValue().toString());
        }

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                taskDispatcher.addMasterChangeListener(masterChangeListener);
            }
        }
    }

    /**
     * 可以自己得到taskDispatcher对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            taskDispatcher.start();
            started = true;
        }
    }

    @Override
    public void destroy() throws Exception {
        taskDispatcher.stop();
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setMasterChangeListeners(MasterChangeListener[] masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }

	public Integer getListenPort() {
		return listenPort;
	}

	public void setListenPort(Integer listenPort) {
		this.listenPort = listenPort;
	}
    
}