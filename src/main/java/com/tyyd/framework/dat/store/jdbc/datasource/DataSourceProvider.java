package com.tyyd.framework.dat.store.jdbc.datasource;

import com.tyyd.framework.dat.core.cluster.Config;

import javax.sql.DataSource;

/**
 * @author   on 10/24/14.
 */
public interface DataSourceProvider {

    String H2 = "h2";

    String MYSQL = "mysql";

    DataSource getDataSource(Config config);

}
