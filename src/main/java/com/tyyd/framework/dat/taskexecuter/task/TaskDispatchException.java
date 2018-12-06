package com.tyyd.framework.dat.taskexecuter.task;

public class TaskDispatchException extends Exception{

	private static final long serialVersionUID = -99670791735250890L;

	public TaskDispatchException() {
        super();
    }

    public TaskDispatchException(String message) {
        super(message);
    }

    public TaskDispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskDispatchException(Throwable cause) {
        super(cause);
    }
}
