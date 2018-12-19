package com.tyyd.framework.dat.core.listener;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.ec.EventInfo;

import java.util.List;

/**
 * 用来监听自己的节点信息变化
 */
public class SelfChangeListener implements NodeChangeListener {

    private Config config;
    private AppContext appContext;

    public SelfChangeListener(AppContext appContext) {
        this.config = appContext.getConfig();
        this.appContext = appContext;
    }


    private void change(Node node) {
        if (node.getIdentity().equals(config.getIdentity())) {
            // 是当前节点, 看看节点配置是否发生变化
            // 1. 看 threads 有没有改变 , 目前只有 TASK_TRACKER 对 threads起作用
            if (node.getNodeType().equals(NodeType.TASK_EXECUTER)
                    && (node.getThreads() != config.getWorkThreads())) {
                config.setWorkThreads(node.getThreads());
                appContext.getEventCenter().publishAsync(new EventInfo(EcTopic.WORK_THREAD_CHANGE));
            }
        }
    }

    @Override
    public void addNodes(List<Node> nodes) {
        if (CollectionUtils.isEmpty(nodes)) {
            return;
        }
        for (Node node : nodes) {
            change(node);
        }
    }

    @Override
    public void removeNodes(List<Node> nodes) {

    }


	@Override
	public void updateNodes(List<Node> nodes) {
		
	}
}
