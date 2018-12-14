package com.tyyd.framework.dat.core.failstore;

public class FailStoreException extends Exception {

	private static final long serialVersionUID = -527169987823345068L;

	public FailStoreException(String message) {
        super(message);
    }

    public FailStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public FailStoreException(Throwable cause) {
        super(cause);
    }

}
