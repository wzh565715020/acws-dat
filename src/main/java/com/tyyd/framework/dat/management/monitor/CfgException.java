package com.tyyd.framework.dat.management.monitor;

public class CfgException extends RuntimeException {

    public CfgException(String message) {
        super(message);
    }

    public CfgException(String message, Throwable cause) {
        super(message, cause);
    }

    public CfgException(Throwable cause) {
        super(cause);
    }
}
