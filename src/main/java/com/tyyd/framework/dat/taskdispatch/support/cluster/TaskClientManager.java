package com.tyyd.framework.dat.taskdispatch.support.cluster;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.loadbalance.LoadBalance;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelWrapper;
import com.tyyd.framework.dat.taskdispatch.domain.TaskClientNode;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 *         客户端节点管理
 */
public class TaskClientManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskClientManager.class);

    private final ConcurrentHashMap<String/*nodeGroup*/, Set<TaskClientNode>> NODE_MAP = new ConcurrentHashMap<String, Set<TaskClientNode>>();

    private LoadBalance loadBalance;
    private TaskDispatcherAppContext appContext;

    public TaskClientManager(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig());
    }

    /**
     * get all connected node group
     */
    public Set<String> getNodeGroups() {
        return NODE_MAP.keySet();
    }

    /**
     * 添加节点
     */
    public void addNode(Node node) {
        //  channel 可能为 null
        ChannelWrapper channel = appContext.getChannelManager().getChannel(node.getGroup(), node.getNodeType(), node.getIdentity());
        Set<TaskClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());

        if (jobClientNodes == null) {
            jobClientNodes = new ConcurrentHashSet<TaskClientNode>();
            Set<TaskClientNode> oldSet = NODE_MAP.putIfAbsent(node.getGroup(), jobClientNodes);
            if (oldSet != null) {
                jobClientNodes = oldSet;
            }
        }

        TaskClientNode jobClientNode = new TaskClientNode(node.getGroup(), node.getIdentity(), channel);
        LOGGER.info("add JobClient node:{}", jobClientNode);
        jobClientNodes.add(jobClientNode);

        // create feedback queue
        appContext.getJobFeedbackQueue().createQueue(node.getGroup());
        appContext.getNodeGroupStore().addNodeGroup(NodeType.JOB_CLIENT, node.getGroup());
    }

    /**
     * 删除节点
     */
    public void removeNode(Node node) {
        Set<TaskClientNode> jobClientNodes = NODE_MAP.get(node.getGroup());
        if (jobClientNodes != null && jobClientNodes.size() != 0) {
            for (TaskClientNode jobClientNode : jobClientNodes) {
                if (node.getIdentity().equals(jobClientNode.getIdentity())) {
                    LOGGER.info("remove JobClient node:{}", jobClientNode);
                    jobClientNodes.remove(jobClientNode);
                }
            }
        }
    }

    /**
     * 得到 可用的 客户端节点
     */
    public TaskClientNode getAvailableJobClient(String nodeGroup) {

        Set<TaskClientNode> jobClientNodes = NODE_MAP.get(nodeGroup);

        if (CollectionUtils.isEmpty(jobClientNodes)) {
            return null;
        }

        List<TaskClientNode> list = new ArrayList<TaskClientNode>(jobClientNodes);

        while (list.size() > 0) {

            TaskClientNode jobClientNode = loadBalance.select(list, null);

            if (jobClientNode != null && (jobClientNode.getChannel() == null || jobClientNode.getChannel().isClosed())) {
                ChannelWrapper channel = appContext.getChannelManager().getChannel(jobClientNode.getNodeGroup(), NodeType.JOB_CLIENT, jobClientNode.getIdentity());
                if (channel != null) {
                    // 更新channel
                    jobClientNode.setChannel(channel);
                }
            }

            if (jobClientNode != null && jobClientNode.getChannel() != null && !jobClientNode.getChannel().isClosed()) {
                return jobClientNode;
            } else {
                list.remove(jobClientNode);
            }
        }
        return null;
    }

}
