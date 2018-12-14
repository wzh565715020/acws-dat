package com.tyyd.framework.dat.management.support;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.tyyd.framework.dat.biz.logger.SmartJobLogger;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.utils.BeanUtils;
import com.tyyd.framework.dat.core.commons.utils.NetUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.registry.RegistryStatMonitor;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.ec.EventCenter;
import com.tyyd.framework.dat.management.access.BackendAccessFactory;
import com.tyyd.framework.dat.management.access.memory.NodeMemCacheAccess;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.cluster.BackendNode;
import com.tyyd.framework.dat.queue.TaskQueueFactory;

import java.util.Map;


public class BackendAppContextFactoryBean implements FactoryBean<BackendAppContext>, InitializingBean {

    private BackendAppContext appContext;

    @Override
    public BackendAppContext getObject() throws Exception {
        return appContext;
    }

    @Override
    public Class<?> getObjectType() {
        return BackendAppContext.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final Node node = new BackendNode();
        node.setCreateTime(SystemClock.now());
        node.setIp(NetUtils.getLocalHost());
        node.setHostName(NetUtils.getLocalHostName());
        node.setIdentity(Constants.ADMIN_ID_PREFIX + StringUtils.generateUUID());

        Config config = new Config();
        config.setIdentity(node.getIdentity());
        config.setNodeType(node.getNodeType());
        config.setRegistryAddress(AppConfigurer.getProperty("registryAddress"));
        String clusterName = AppConfigurer.getProperty("clusterName");
        if (StringUtils.isEmpty(clusterName)) {
            throw new IllegalArgumentException("clusterName in lts-admin.cfg can not be null.");
        }
        config.setClusterName(clusterName);

        Config jobTConfig = new Config();

        for (Map.Entry<String, String> entry : AppConfigurer.allConfig().entrySet()) {
            // 将 config. 开头的配置都加入到config中
            if (entry.getKey().startsWith("configs.")) {
                config.setParameter(entry.getKey().replaceFirst("configs.", ""), entry.getValue());
            } else if (entry.getKey().startsWith("jobT.")) {
                jobTConfig.setParameter(entry.getKey().replace("jobT.", ""), entry.getValue());
            }
        }

        appContext = new BackendAppContext();
        appContext.setConfig(config);
        appContext.setNode(node);
        appContext.setEventCenter(ServiceLoader.load(EventCenter.class, config));
        appContext.setRegistryStatMonitor(new RegistryStatMonitor(appContext));

        initAccess(config);

        // ----------------------下面是JobQueue的配置---------------------------
        jobTConfig = (Config) BeanUtils.deepClone(config);
        for (Map.Entry<String, String> entry : AppConfigurer.allConfig().entrySet()) {
            // 将 jobT. 开头的配置都加入到jobTConfig中
            if (entry.getKey().startsWith("jobT.")) {
                jobTConfig.setParameter(entry.getKey().replace("jobT.", ""), entry.getValue());
            }
        }
        initJobQueue(jobTConfig);
    }

    private void initJobQueue(Config config) {
        TaskQueueFactory factory = ServiceLoader.load(TaskQueueFactory.class, config);
        appContext.setExecutableJobQueue(factory.getExecutableJobQueue(config));
        appContext.setExecutingJobQueue(factory.getExecutingJobQueue(config));
        appContext.setTaskQueue(factory.getTaskQueue(config));
        appContext.setJobLogger(new SmartJobLogger(appContext));
    }

    private void initAccess(Config config) {
        BackendAccessFactory factory = ServiceLoader.load(BackendAccessFactory.class, config);
        appContext.setBackendJobClientMAccess(factory.getBackendJobClientMAccess(config));
        appContext.setBackendJobTrackerMAccess(factory.getJobTrackerMAccess(config));
        appContext.setBackendTaskTrackerMAccess(factory.getBackendTaskTrackerMAccess(config));
        appContext.setBackendJVMGCAccess(factory.getBackendJVMGCAccess(config));
        appContext.setBackendJVMMemoryAccess(factory.getBackendJVMMemoryAccess(config));
        appContext.setBackendJVMThreadAccess(factory.getBackendJVMThreadAccess(config));
        appContext.setBackendNodeOnOfflineLogAccess(factory.getBackendNodeOnOfflineLogAccess(config));
        appContext.setNodeMemCacheAccess(new NodeMemCacheAccess());
    }

}
