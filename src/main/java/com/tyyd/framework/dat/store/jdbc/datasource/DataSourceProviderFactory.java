package com.tyyd.framework.dat.store.jdbc.datasource;

import com.tyyd.framework.dat.core.cluster.Config;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author   on 6/6/15.
 */
public class DataSourceProviderFactory {

    private static final ConcurrentHashMap<String, DataSourceProvider> PROVIDER_MAP = new ConcurrentHashMap<String, DataSourceProvider>();

    static {
        PROVIDER_MAP.put(DataSourceProvider.MYSQL, new MysqlDataSourceProvider());
    }

    public static DataSourceProvider create(Config config) {
        String provider = config.getParameter("jdbc.datasource.provider", DataSourceProvider.MYSQL);
        DataSourceProvider dataSourceProvider = PROVIDER_MAP.get(provider);
        if (dataSourceProvider == null) {
            throw new IllegalArgumentException("Can not find jdbc.datasource.provider:" + provider);
        }
        return dataSourceProvider;
    }

}
