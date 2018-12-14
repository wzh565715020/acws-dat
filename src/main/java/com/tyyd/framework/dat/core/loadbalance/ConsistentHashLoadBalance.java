package com.tyyd.framework.dat.core.loadbalance;

import com.tyyd.framework.dat.core.commons.concurrent.ThreadLocalRandom;
import com.tyyd.framework.dat.core.support.ConsistentHashSelector;

import java.util.List;

/**
 * 一致性hash算法
 */
public class ConsistentHashLoadBalance extends AbstractLoadBalance {

    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
        if(seed == null || seed.length() == 0){
            seed = "HASH-".concat(String.valueOf(ThreadLocalRandom.current().nextInt()));
        }
        ConsistentHashSelector<S> selector = new ConsistentHashSelector<S>(shards);
        return selector.selectForKey(seed);
    }
}
