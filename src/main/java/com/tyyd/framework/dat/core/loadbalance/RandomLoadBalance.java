package com.tyyd.framework.dat.core.loadbalance;

import com.tyyd.framework.dat.core.commons.concurrent.ThreadLocalRandom;

import java.util.List;

/**
 * 随机负载均衡算法
 */
public class RandomLoadBalance extends AbstractLoadBalance {

    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
        return shards.get(ThreadLocalRandom.current().nextInt(shards.size()));
    }
}
