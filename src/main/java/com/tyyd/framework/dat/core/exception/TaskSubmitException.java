package com.tyyd.framework.dat.core.exception;

public class TaskSubmitException extends RuntimeException {

	private static final long serialVersionUID = 8375498515729588730L;

	public TaskSubmitException() {
        super();
    }

    public TaskSubmitException(String message) {
        super(message);
    }

    public TaskSubmitException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskSubmitException(Throwable cause) {
        super(cause);
    }

}
