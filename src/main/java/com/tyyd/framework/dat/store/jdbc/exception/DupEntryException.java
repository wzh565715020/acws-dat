package com.tyyd.framework.dat.store.jdbc.exception;

public class DupEntryException extends JdbcException {

    public DupEntryException() {
        super();
    }

    public DupEntryException(String message) {
        super(message);
    }

    public DupEntryException(String message, Throwable cause) {
        super(message, cause);
    }

    public DupEntryException(Throwable cause) {
        super(cause);
    }
}
