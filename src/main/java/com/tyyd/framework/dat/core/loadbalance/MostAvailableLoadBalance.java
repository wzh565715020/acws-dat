package com.tyyd.framework.dat.core.loadbalance;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.tyyd.framework.dat.core.cluster.Node;

public class MostAvailableLoadBalance extends AbstractLoadBalance{
    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
    	if (shards.get(0) instanceof Node) {
            Collections.sort(shards, new Comparator<S>() {
                @Override
                public int compare(S left, S right) {
                    return -((Node) left).getAvailableThreads().compareTo(((Node) right).getAvailableThreads());
                }
            });
        }

        return shards.get(0);
    }
}
