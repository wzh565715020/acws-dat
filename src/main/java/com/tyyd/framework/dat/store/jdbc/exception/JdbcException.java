package com.tyyd.framework.dat.store.jdbc.exception;

public class JdbcException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1639218596520513320L;

	public JdbcException() {
        super();
    }

    public JdbcException(String message) {
        super(message);
    }

    public JdbcException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcException(Throwable cause) {
        super(cause);
    }

}
