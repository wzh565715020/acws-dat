package com.tyyd.framework.dat.taskdispatch.support.listener;


import java.util.List;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.listener.NodeChangeListener;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 *         节点变化监听器
 */
public class TaskNodeChangeListener implements NodeChangeListener {

    private TaskDispatcherAppContext appContext;

    public TaskNodeChangeListener(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
                appContext.getTaskTrackerManager().addNode(node);
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
                appContext.getJobClientManager().addNode(node);
            }
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_TRACKER)) {
                appContext.getTaskTrackerManager().removeNode(node);
            } else if (node.getNodeType().equals(NodeType.JOB_CLIENT)) {
                appContext.getJobClientManager().removeNode(node);
            }
        }
    }
}
