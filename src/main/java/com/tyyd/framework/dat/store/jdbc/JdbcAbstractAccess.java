package com.tyyd.framework.dat.store.jdbc;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.exception.LtsRuntimeException;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Robert HG (254963746@qq.com) on 5/19/15.
 */
public abstract class JdbcAbstractAccess {

    private SqlTemplate sqlTemplate;

    public JdbcAbstractAccess(Config config) {
        this.sqlTemplate = SqlTemplateFactory.create(config);
    }

    public SqlTemplate getSqlTemplate() {
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

    protected String readSqlFile(String path, String tableName) {
        String sql = readSqlFile(path);
        return sql.replace("{tableName}", tableName);
    }

    protected void createTable(String sql) throws JdbcException {
        try {
            getSqlTemplate().createTable(sql);
        } catch (Exception e) {
            throw new JdbcException("Create table error, sql=" + sql, e);
        }
    }
}
