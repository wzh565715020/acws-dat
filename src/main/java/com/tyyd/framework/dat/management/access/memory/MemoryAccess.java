package com.tyyd.framework.dat.management.access.memory;


import java.io.IOException;
import java.io.InputStream;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.exception.LtsRuntimeException;
import com.tyyd.framework.dat.store.jdbc.SqlTemplate;
import com.tyyd.framework.dat.store.jdbc.SqlTemplateFactory;
import com.tyyd.framework.dat.store.jdbc.datasource.DataSourceProvider;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;

/**
 * Memory-Only Databases , HSQLDB
 *
 */
public abstract class MemoryAccess {

    private SqlTemplate sqlTemplate;

    public MemoryAccess() {
        Config config = new Config();
        config.setParameter("jdbc.datasource.provider", DataSourceProvider.H2);
        // see http://www.h2database.com/html/features.html#in_memory_databases
        config.setParameter("jdbc.url", "jdbc:h2:mem:lts_admin;DB_CLOSE_DELAY=-1");
        config.setParameter("jdbc.username", "lts");
        config.setParameter("jdbc.password", "lts");
        sqlTemplate = SqlTemplateFactory.create(config);
    }

    protected SqlTemplate getSqlTemplate() {
        return sqlTemplate;
    }

    protected String readSqlFile(String path) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            return FileUtils.read(is, Constants.CHARSET);
        } catch (IOException e) {
            throw new LtsRuntimeException("Read sql file : [" + path + "] error ", e);
        }
    }

    protected void createTable(String sql) throws JdbcException {
        try {
            getSqlTemplate().createTable(sql);
        } catch (Exception e) {
            throw new JdbcException("Create table error, sql=" + sql, e);
        }
    }
}
