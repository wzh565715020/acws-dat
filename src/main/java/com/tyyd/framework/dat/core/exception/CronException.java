package com.tyyd.framework.dat.core.exception;

public class CronException extends RuntimeException {

	private static final long serialVersionUID = -5252237483450100864L;

	public CronException() {
        super();
    }

    public CronException(String message) {
        super(message);
    }

    public CronException(String message, Throwable cause) {
        super(message, cause);
    }

    public CronException(Throwable cause) {
        super(cause);
    }

}
