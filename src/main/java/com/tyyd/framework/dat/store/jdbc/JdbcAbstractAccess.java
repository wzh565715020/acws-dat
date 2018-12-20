package com.tyyd.framework.dat.store.jdbc;


public abstract class JdbcAbstractAccess {

    private SqlTemplate sqlTemplate;

    public JdbcAbstractAccess() {
        this.sqlTemplate = SqlTemplateFactory.create();
    }

    public SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }
}
