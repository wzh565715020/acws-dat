package com.tyyd.framework.dat.management.monitor.access.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.store.jdbc.JdbcAbstractAccess;

public abstract class MysqlAbstractJdbcAccess extends JdbcAbstractAccess {

    public MysqlAbstractJdbcAccess(Config config) {
        super(config);
    }

    protected abstract String getTableName();

}
