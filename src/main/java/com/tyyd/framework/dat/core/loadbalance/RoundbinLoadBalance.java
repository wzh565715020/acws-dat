package com.tyyd.framework.dat.core.loadbalance;

import java.util.List;

public class RoundbinLoadBalance extends AbstractLoadBalance{
    @Override
    protected <S> S doSelect(List<S> shards, String seed) {
        // TODO
        return null;
    }
}
