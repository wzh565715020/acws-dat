package com.tyyd.framework.dat.store.jdbc;

import com.tyyd.framework.dat.core.cluster.Config;

public abstract class JdbcAbstractAccess {

    private SqlTemplate sqlTemplate;

    public JdbcAbstractAccess(Config config) {
        this.sqlTemplate = SqlTemplateFactory.create(config);
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }
}
