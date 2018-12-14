package com.tyyd.framework.dat.core.failstore.mapdb;

import com.tyyd.framework.dat.core.failstore.AbstractFailStoreFactory;
import com.tyyd.framework.dat.core.failstore.FailStore;

import java.io.File;
public class MapdbFailStoreFactory extends AbstractFailStoreFactory {

    @Override
    protected String getName() {
        return MapdbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new MapdbFailStore(dbPath, needLock);
    }
}
