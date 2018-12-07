package com.tyyd.framework.dat.management.monitor;

import com.tyyd.framework.dat.cmd.HttpCmdServer;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cmd.JVMInfoGetHttpCmd;
import com.tyyd.framework.dat.core.cmd.StatusCheckHttpCmd;
import com.tyyd.framework.dat.core.commons.utils.NetUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.factory.JobNodeConfigFactory;
import com.tyyd.framework.dat.core.factory.NodeFactory;
import com.tyyd.framework.dat.core.json.JSONFactory;
import com.tyyd.framework.dat.core.registry.AbstractRegistry;
import com.tyyd.framework.dat.core.registry.Registry;
import com.tyyd.framework.dat.core.registry.RegistryFactory;
import com.tyyd.framework.dat.core.registry.RegistryStatMonitor;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;
import com.tyyd.framework.dat.core.support.AliveKeeping;
import com.tyyd.framework.dat.ec.EventCenter;
import com.tyyd.framework.dat.jvmmonitor.JVMMonitor;
import com.tyyd.framework.dat.management.monitor.access.MonitorAccessFactory;
import com.tyyd.framework.dat.management.monitor.cmd.MDataAddHttpCmd;
import com.tyyd.framework.dat.management.monitor.cmd.MDataSrv;

public class MonitorAgent {

    private HttpCmdServer httpCmdServer;
    private MonitorAppContext appContext;
    private Config config;
    private Registry registry;
    private MonitorNode node;

    public MonitorAgent() {
        this.appContext = new MonitorAppContext();
        this.config = JobNodeConfigFactory.getDefaultConfig();
        this.appContext.setConfig(config);
    }

    public void start() {

        // 初始化
        intConfig();

        // 默认端口
        int port = config.getParameter("lts.http.cmd.port", 8730);
        this.httpCmdServer = HttpCmdServer.Factory.getHttpCmdServer(config.getIp(), port);

        this.httpCmdServer.registerCommands(
                new MDataAddHttpCmd(this.appContext),
                new StatusCheckHttpCmd(config),
                new JVMInfoGetHttpCmd(config));
        // 启动
        this.httpCmdServer.start();

        // 设置真正启动的端口
        this.appContext.setHttpCmdPort(httpCmdServer.getPort());

        initNode();

        // 暴露在 zk 上
        initRegistry();
        registry.register(node);

        JVMMonitor.start();
        AliveKeeping.start();
    }

    public void initRegistry() {
        registry = RegistryFactory.getRegistry(appContext);
        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(node);
        }
    }

    private void initNode() {

        if (StringUtils.isEmpty(config.getIp())) {
            config.setIp(NetUtils.getLocalHost());
        }
        config.setListenPort(this.appContext.getHttpCmdPort());
        this.node = NodeFactory.create(MonitorNode.class, config);
        this.node.setHttpCmdPort(this.appContext.getHttpCmdPort());
        this.config.setNodeType(node.getNodeType());
    }

    private void intConfig() {
        // 初始化一些 db access
        MonitorAccessFactory factory = ServiceLoader.load(MonitorAccessFactory.class, config);
        this.appContext.setJobTrackerMAccess(factory.getJobTrackerMAccess(config));
        this.appContext.setJvmGCAccess(factory.getJVMGCAccess(config));
        this.appContext.setJvmMemoryAccess(factory.getJVMMemoryAccess(config));
        this.appContext.setJvmThreadAccess(factory.getJVMThreadAccess(config));
        this.appContext.setTaskTrackerMAccess(factory.getTaskTrackerMAccess(config));
        this.appContext.setJobClientMAccess(factory.getJobClientMAccess(config));

        this.appContext.setMDataSrv(new MDataSrv(this.appContext));

        this.appContext.setEventCenter(ServiceLoader.load(EventCenter.class, config));
        this.appContext.setRegistryStatMonitor(new RegistryStatMonitor(appContext));

        // 设置json
        String ltsJson = config.getParameter(SpiExtensionKey.DAT_JSON);
        if (StringUtils.isNotEmpty(ltsJson)) {
            JSONFactory.setJSONAdapter(ltsJson);
        }
    }

    public void stop() {
        // 先取消暴露
        this.registry.unregister(node);
        // 停止服务
        this.httpCmdServer.stop();

        JVMMonitor.stop();
        AliveKeeping.stop();
    }

    /**
     * 设置集群名字
     */
    public void setClusterName(String clusterName) {
        config.setClusterName(clusterName);
    }

    /**
     * 设置zookeeper注册中心地址
     */
    public void setRegistryAddress(String registryAddress) {
        config.setRegistryAddress(registryAddress);
    }

    /**
     * 设置额外的配置参数
     */
    public void addConfig(String key, String value) {
        config.setParameter(key, value);
    }

    /**
     * 节点标识(必须要保证这个标识是唯一的才能设置，请谨慎设置)
     * 这个是非必须设置的，建议使用系统默认生成
     */
    public void setIdentity(String identity) {
        config.setIdentity(identity);
    }

    /**
     * 显示设置绑定ip
     */
    public void setBindIp(String bindIp) {
        if (StringUtils.isEmpty(bindIp)
                || !NetUtils.isValidHost(bindIp)
                ) {
            throw new IllegalArgumentException("Invalided bind ip:" + bindIp);
        }
        config.setIp(bindIp);
    }
}
