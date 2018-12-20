package com.tyyd.framework.dat.core.loadbalance;

import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

import java.util.List;

@SPI(key = SpiExtensionKey.LOADBALANCE, dftValue = "consistenthash")
public interface LoadBalance {

    public <S> S select(List<S> shards, String seed);

}
