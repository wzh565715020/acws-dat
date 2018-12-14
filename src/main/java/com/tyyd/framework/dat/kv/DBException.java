package com.tyyd.framework.dat.kv;

/**
 * @author   on 12/13/15.
 */
public class DBException extends RuntimeException {

    public DBException() {
        super();
    }

    public DBException(String s) {
        super(s);
    }

    public DBException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public DBException(Throwable throwable) {
        super(throwable);
    }
}
