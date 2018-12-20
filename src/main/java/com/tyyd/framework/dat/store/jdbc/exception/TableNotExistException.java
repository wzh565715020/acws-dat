package com.tyyd.framework.dat.store.jdbc.exception;

public class TableNotExistException extends JdbcException {

    public TableNotExistException() {
        super();
    }

    public TableNotExistException(String message) {
        super(message);
    }

    public TableNotExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableNotExistException(Throwable cause) {
        super(cause);
    }
}
