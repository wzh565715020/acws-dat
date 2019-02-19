package com.tyyd.framework.dat.core.exception;

public class RequestTimeoutException extends RuntimeException {

	private static final long serialVersionUID = 7216536669163975612L;

	public RequestTimeoutException() {
        super();
    }

    public RequestTimeoutException(String message) {
        super(message);
    }

    public RequestTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestTimeoutException(Throwable cause) {
        super(cause);
    }

}
