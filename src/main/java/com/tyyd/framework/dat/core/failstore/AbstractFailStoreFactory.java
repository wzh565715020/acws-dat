package com.tyyd.framework.dat.core.failstore;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;

import java.io.File;
import java.io.IOException;

public abstract class AbstractFailStoreFactory implements FailStoreFactory {
    @Override
    public final FailStore getFailStore(Config config, String storePath) {
        if (StringUtils.isEmpty(storePath)) {
            storePath = config.getFailStorePath();
        }
        File dbPath = new File(storePath.concat(getName()).concat("/").concat(config.getIdentity()));
        try {
            FileUtils.createDirIfNotExist(dbPath);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return newInstance(dbPath, true);
    }

    protected abstract String getName();

    protected abstract FailStore newInstance(File dbPath, boolean needLock);
}
