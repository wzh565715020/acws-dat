package com.tyyd.framework.dat.core.failstore.rocksdb;

import com.tyyd.framework.dat.core.failstore.AbstractFailStoreFactory;
import com.tyyd.framework.dat.core.failstore.FailStore;

import java.io.File;

public class RocksdbFailStoreFactory extends AbstractFailStoreFactory{

    @Override
    protected String getName() {
        return RocksdbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new RocksdbFailStore(dbPath, needLock);
    }
}
