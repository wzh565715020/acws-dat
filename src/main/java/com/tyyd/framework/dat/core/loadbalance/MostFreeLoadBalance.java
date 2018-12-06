package com.tyyd.framework.dat.core.loadbalance;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.taskdispatch.domain.TaskExecuterNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 连接剩余线程数最多的机器 或者接最老的一台TaskTrackerNode,从而达到主从模式的效果
 *
 */
public class MostFreeLoadBalance extends AbstractLoadBalance {

    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
		if (shards.get(0) instanceof TaskExecuterNode) {
			Collections.sort(shards, new Comparator<S>() {
                @Override
                public int compare(S left, S right) {
                    return ((TaskExecuterNode) left).getAvailableThreadInteger().compareTo(((TaskExecuterNode) right).getAvailableThreadInteger());
                }
            });
		}else if (shards.get(0) instanceof Node) {
            Collections.sort(shards, new Comparator<S>() {
                @Override
                public int compare(S left, S right) {
                    return ((Node) left).getCreateTime().compareTo(((Node) right).getCreateTime());
                }
            });
        }

        return shards.get(0);
    }
}
