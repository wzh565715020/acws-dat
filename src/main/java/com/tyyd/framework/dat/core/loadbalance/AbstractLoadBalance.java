package com.tyyd.framework.dat.core.loadbalance;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {

    @Override
    public <S> S select(List<S> shards, String seed) {
        if (shards == null || shards.size() == 0) {
            return null;
        }

        if (shards.size() == 1) {
            return shards.get(0);
        }

        return doSelect(shards, seed);
    }

    protected abstract <S> S doSelect(List<S> shards, String seed);

}
