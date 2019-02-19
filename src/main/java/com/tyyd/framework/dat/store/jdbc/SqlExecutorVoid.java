package com.tyyd.framework.dat.store.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlExecutorVoid {
    void run(Connection conn) throws SQLException;
}
