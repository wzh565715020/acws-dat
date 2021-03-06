package com.tyyd.framework.dat.core.exception;

public class RemotingSendException extends Exception{

	private static final long serialVersionUID = -8901776781734789960L;

	public RemotingSendException() {
        super();
    }

    public RemotingSendException(String message) {
        super(message);
    }

    public RemotingSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public RemotingSendException(Throwable cause) {
        super(cause);
    }

}
