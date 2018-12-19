package com.tyyd.framework.dat.store.mongo;

import com.tyyd.framework.dat.core.cluster.Config;
import org.mongodb.morphia.AdvancedDatastore;

/**
 *         通用的mongo存储类
 */
public abstract class MongoRepository {

    protected final MongoTemplate template;

    public MongoRepository(Config config) {
        this.template = new MongoTemplate(
                (AdvancedDatastore) DataStoreProvider.getDataStore(config));
    }

    public MongoTemplate getTemplate() {
        return template;
    }

    public String getTableName() {
        return template.getDefaultCollName();
    }

    public void setTableName(String tableName) {
        template.setDefaultCollName(tableName);
    }
}