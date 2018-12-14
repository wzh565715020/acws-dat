package com.tyyd.framework.dat.store.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author   on 5/20/15.
 */
public interface SqlExecutorVoid {
    void run(Connection conn) throws SQLException;
}
