package com.tyyd.framework.dat.store.jdbc.builder;

import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.store.jdbc.SQLFormatter;
import com.tyyd.framework.dat.store.jdbc.SqlTemplate;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;

/**
 * @author   on 3/9/16.
 */
public class DropTableSql {

    private static final Logger LOGGER = LoggerFactory.getLogger(DropTableSql.class);

    private SqlTemplate sqlTemplate;
    private StringBuilder sql = new StringBuilder();

    public DropTableSql(SqlTemplate sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    public DropTableSql drop(String table) {
        sql.append("DROP TABLE IF EXISTS ").append(table);
        return this;
    }

    public boolean doDrop() {

        String finalSQL = sql.toString();

        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(SQLFormatter.format(finalSQL));
            }

            sqlTemplate.update(sql.toString());
        } catch (Exception e) {
            throw new JdbcException("Drop Table Error:" + SQLFormatter.format(finalSQL), e);
        }
        return true;
    }

}
