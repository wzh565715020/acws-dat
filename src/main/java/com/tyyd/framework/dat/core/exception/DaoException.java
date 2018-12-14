package com.tyyd.framework.dat.core.exception;

public class DaoException extends RuntimeException{

	private static final long serialVersionUID = -4031278211419963345L;

	public DaoException() {
        super();
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }
}
