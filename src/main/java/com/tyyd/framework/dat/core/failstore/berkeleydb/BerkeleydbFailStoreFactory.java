package com.tyyd.framework.dat.core.failstore.berkeleydb;

import com.tyyd.framework.dat.core.failstore.AbstractFailStoreFactory;
import com.tyyd.framework.dat.core.failstore.FailStore;

import java.io.File;

public class BerkeleydbFailStoreFactory extends AbstractFailStoreFactory{

    @Override
    protected String getName() {
        return BerkeleydbFailStore.name;
    }

    @Override
    protected FailStore newInstance(File dbPath, boolean needLock) {
        return new BerkeleydbFailStore(dbPath, needLock);
    }
}
