package com.tyyd.framework.dat.taskexecuter;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.tyyd.framework.dat.core.commons.utils.Assert;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.listener.MasterChangeListener;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * TaskTracker Spring Bean 工厂类
 * 如果用这个工厂类，那么JobRunner中引用SpringBean的话,只有通过xml的方式注入
 */
public abstract class TaskExecuterXmlFactoryBean implements FactoryBean<TaskExecuter>,
        InitializingBean, DisposableBean {

    private TaskExecuter taskExecuter;
    
    private boolean started;
    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 节点组名称
     */
    private String nodeGroup;
    /**
     * zookeeper地址
     */
    private String registryAddress;
    /**
     * 提交失败任务存储路径 , 默认用户木邻居
     */
    private String dataPath;
    /**
     * 工作线程个数
     */
    private int workThreads;
    /**
     * 业务日志级别
     */
    private Level bizLoggerLevel;
    /**
     * master节点变化监听器
     */
    private MasterChangeListener[] masterChangeListeners;
    /**
     * 额外参数配置
     */
    private Properties configs = new Properties();

    @Override
    public TaskExecuter getObject() throws Exception {
        return taskExecuter;
    }

    @Override
    public Class<?> getObjectType() {
        return TaskExecuter.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }


    public void checkProperties() {
        Assert.hasText(clusterName, "clusterName must have value.");
        Assert.hasText(nodeGroup, "nodeGroup must have value.");
        Assert.hasText(registryAddress, "registryAddress must have value.");
        Assert.isTrue(workThreads > 0, "workThreads must > 0.");
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        checkProperties();

        taskExecuter = new TaskExecuter();

        taskExecuter.setClusterName(clusterName);
        taskExecuter.setDataPath(dataPath);
        taskExecuter.setWorkThreads(workThreads);
        taskExecuter.setNodeGroup(nodeGroup);
        taskExecuter.setRegistryAddress(registryAddress);

        if (bizLoggerLevel != null) {
            taskExecuter.setBizLoggerLevel(bizLoggerLevel);
        }

        // 设置config
        for (Map.Entry<Object, Object> entry : configs.entrySet()) {
            taskExecuter.addConfig(entry.getKey().toString(), entry.getValue().toString());
        }

        taskExecuter.setRunnerFactory(new RunnerFactory() {
            @Override
            public TaskRunner newRunner() {
                return createJobRunner();
            }
        });

        if (masterChangeListeners != null) {
            for (MasterChangeListener masterChangeListener : masterChangeListeners) {
                taskExecuter.addMasterChangeListener(masterChangeListener);
            }
        }

    }

    /**
     * 可以自己得到TaskTracker对象后调用，也可以直接使用spring配置中的init属性指定该方法
     */
    public void start() {
        if (!started) {
            taskExecuter.start();
            started = true;
        }
    }

    protected abstract TaskRunner createJobRunner();

    @Override
    public void destroy() throws Exception {
        taskExecuter.stop();
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public void setWorkThreads(int workThreads) {
        this.workThreads = workThreads;
    }

    public void setMasterChangeListeners(MasterChangeListener[] masterChangeListeners) {
        this.masterChangeListeners = masterChangeListeners;
    }

    public void setBizLoggerLevel(String bizLoggerLevel) {
        if (StringUtils.isNotEmpty(bizLoggerLevel)) {
            this.bizLoggerLevel = Level.valueOf(bizLoggerLevel);
        }
    }

    public void setConfigs(Properties configs) {
        this.configs = configs;
    }
}
