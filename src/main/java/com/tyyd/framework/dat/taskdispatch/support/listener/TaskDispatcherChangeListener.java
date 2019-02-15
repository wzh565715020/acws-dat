package com.tyyd.framework.dat.taskdispatch.support.listener;


import java.util.List;

import org.springframework.util.CollectionUtils;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.listener.NodeChangeListener;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskDispatcherChangeListener implements NodeChangeListener {

    private TaskDispatcherAppContext appContext;

    public TaskDispatcherChangeListener(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_DISPATCH)) {
                appContext.getTaskDispatcherManager().addNode(node);
            } 
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            if (node.getNodeType().equals(NodeType.TASK_DISPATCH)) {
                appContext.getTaskDispatcherManager().removeNode(node);
            } 
        }
    }


	@Override
	public void updateNodes(List<Node> nodes) {
	}
}
