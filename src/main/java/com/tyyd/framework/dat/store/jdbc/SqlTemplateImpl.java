package com.tyyd.framework.dat.store.jdbc;

import com.tyyd.framework.dat.store.jdbc.dbutils.DbRunner;
import com.tyyd.framework.dat.store.jdbc.dbutils.ResultSetHandler;
import com.tyyd.framework.dat.store.jdbc.dbutils.ScalarHandler;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DataSourceUtils;

import java.sql.Connection;
import java.sql.SQLException;

class SqlTemplateImpl implements SqlTemplate {

    private final DataSource dataSource;
    private final static DbRunner dbRunner = new DbRunner();

    public SqlTemplateImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T execute(boolean isReadOnly, SqlExecutor<T> executor) throws SQLException {
        Connection conn = null;
        try {
            conn = DataSourceUtils.doGetConnection(dataSource); //dataSource.getConnection();
            if (isReadOnly) {
                conn.setReadOnly(true);
            }else {
            	conn.setReadOnly(false);
            }
            return executor.run(conn);
        } finally {
        	DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    public void createTable(final String sql) throws SQLException {
        update(sql);
    }

    @Override
    public int[] batchInsert(String sql, Object[][] params) throws SQLException {
        return batchUpdate(sql, params);
    }

    public int[] batchUpdate(final Connection conn, final String sql, final Object[][] params) throws SQLException {
        return dbRunner.batch(conn, sql, params);
    }

    public int[] batchUpdate(final String sql, final Object[][] params) throws SQLException {
        return execute(false, new SqlExecutor<int[]>() {
            @Override
            public int[] run(Connection conn) throws SQLException {
                return batchUpdate(conn, sql, params);
            }
        });
    }

    @Override
    public int insert(String sql, Object... params) throws SQLException {
        return update(sql, params);
    }

    public int update(final String sql, final Object... params) throws SQLException {
        return execute(false, new SqlExecutor<Integer>() {
            @Override
            public Integer run(Connection conn) throws SQLException {
                return update(conn, sql, params);
            }
        });
    }

    @Override
    public int delete(String sql, Object... params) throws SQLException {
        return update(sql, params);
    }

    public int update(final Connection conn, final String sql, final Object... params) throws SQLException {
        return dbRunner.update(conn, sql, params);
    }

    public <T> T query(final String sql, final ResultSetHandler<T> rsh, final Object... params) throws SQLException {
        return execute(true, new SqlExecutor<T>() {
            @Override
            public T run(Connection conn) throws SQLException {
                return query(conn, sql, rsh, params);
            }
        });
    }

    public <T> T query(final Connection conn, final String sql, final ResultSetHandler<T> rsh, final Object... params) throws SQLException {
        return dbRunner.query(conn, sql, rsh, params);
    }

    public <T> T queryForValue(final String sql, final Object... params) throws SQLException {
        return query(sql, new ScalarHandler<T>(), params);
    }

    public <T> T queryForValue(final Connection conn, final String sql, final Object... params) throws SQLException {
        return query(conn, sql, new ScalarHandler<T>(), params);
    }

    private SqlExecutor<Void> getWrapperExecutor(final SqlExecutorVoid voidExecutor) {
        return new SqlExecutor<Void>() {
            @Override
            public Void run(Connection conn) throws SQLException {
                voidExecutor.run(conn);
                return null;
            }
        };
    }

    public void executeInTransaction(SqlExecutorVoid executor) {
        executeInTransaction(getWrapperExecutor(executor));
    }

    public <T> T executeInTransaction(SqlExecutor<T> executor) {
        Connection conn = null;
        try {
            conn = TxConnectionFactory.getTxConnection(dataSource);
            T res = executor.run(conn);
            conn.commit();
            return res;
        } catch (Error e) {
            throw rollback(conn, e);
        } catch (Exception e) {
            throw rollback(conn, e);
        } finally {
            TxConnectionFactory.closeTx(conn);
        }
    }

    private StateException rollback(Connection conn, Throwable e) {
        try {
            if (conn != null) {
                conn.rollback();
            }
            return new StateException(e);
        } catch (SQLException se) {
            return new StateException("Unable to rollback transaction", e);
        }
    }
}
