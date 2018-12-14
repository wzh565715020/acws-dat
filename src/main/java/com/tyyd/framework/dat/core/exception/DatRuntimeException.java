package com.tyyd.framework.dat.core.exception;

/**
 * @author   on 3/2/16.
 */
public class DatRuntimeException extends RuntimeException {

    public DatRuntimeException() {
        super();
    }

    public DatRuntimeException(String message) {
        super(message);
    }

    public DatRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatRuntimeException(Throwable cause) {
        super(cause);
    }
}
