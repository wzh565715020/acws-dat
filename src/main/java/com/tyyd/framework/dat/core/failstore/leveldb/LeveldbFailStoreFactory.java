package com.tyyd.framework.dat.core.failstore.leveldb;

import com.tyyd.framework.dat.core.failstore.AbstractFailStoreFactory;
import com.tyyd.framework.dat.core.failstore.FailStore;

import java.io.File;

public class LeveldbFailStoreFactory extends AbstractFailStoreFactory {

    @Override
    protected String getName() {
        return LeveldbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new LeveldbFailStore(dbPath, needLock);
    }
}
