package com.tyyd.framework.dat.management.cluster;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.registry.AbstractRegistry;
import com.tyyd.framework.dat.core.registry.NotifyEvent;
import com.tyyd.framework.dat.core.registry.NotifyListener;
import com.tyyd.framework.dat.core.registry.Registry;
import com.tyyd.framework.dat.core.registry.RegistryFactory;
import com.tyyd.framework.dat.management.access.domain.NodeOnOfflineLog;
import com.tyyd.framework.dat.management.request.NodePaginationReq;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 6/5/15.
 */
public class BackendRegistrySrv implements InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackendRegistrySrv.class);
    @Autowired
    private BackendAppContext appContext;
    private Registry registry;
    private NotifyListener notifyListener;

    private void subscribe() {

        if (registry instanceof AbstractRegistry) {
            ((AbstractRegistry) registry).setNode(appContext.getNode());
        }
        registry.subscribe(appContext.getNode(), notifyListener);
    }

    public void reSubscribe() {
        // 取消订阅
        registry.unsubscribe(appContext.getNode(), notifyListener);
        // 清空内存数据
        appContext.getNodeMemCacheAccess().clear();
        // 重新订阅
        subscribe();
    }

    public List<Node> getOnlineNodes(NodePaginationReq request) {
        return appContext.getNodeMemCacheAccess().search(request);
    }

    /**
     * 记录节点上下线日志
     */
    private void addLog(NotifyEvent event, List<Node> nodes) {
        List<NodeOnOfflineLog> logs = new ArrayList<NodeOnOfflineLog>(nodes.size());

        for (Node node : nodes) {
            NodeOnOfflineLog log = new NodeOnOfflineLog();
            log.setLogTime(new Date());
            log.setEvent(event == NotifyEvent.ADD ? "ONLINE" : "OFFLINE");

            log.setClusterName(node.getClusterName());
            log.setCreateTime(node.getCreateTime());
            log.setGroup(node.getGroup());
            log.setHostName(node.getHostName());
            log.setIdentity(node.getIdentity());
            log.setIp(node.getIp());
            log.setPort(node.getPort());
            log.setThreads(node.getThreads());
            log.setNodeType(node.getNodeType());
            log.setHttpCmdPort(node.getHttpCmdPort());

            logs.add(log);
        }

        appContext.getBackendNodeOnOfflineLogAccess().insert(logs);
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        registry = RegistryFactory.getRegistry(appContext);

        notifyListener = new NotifyListener() {
            @Override
            public void notify(NotifyEvent event, List<Node> nodes) {
                if (CollectionUtils.isEmpty(nodes)) {
                    return;
                }
                switch (event) {
                    case ADD:
                        appContext.getNodeMemCacheAccess().addNode(nodes);
                        LOGGER.info("ADD NODE " + nodes);
                        break;
                    case REMOVE:
                        appContext.getNodeMemCacheAccess().removeNode(nodes);
                        LOGGER.info("REMOVE NODE " + nodes);
                        break;
                }
                // 记录日志
                addLog(event, nodes);
            }
        };

        subscribe();
    }
}
