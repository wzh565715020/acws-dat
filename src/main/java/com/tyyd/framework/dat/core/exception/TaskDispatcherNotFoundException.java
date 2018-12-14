package com.tyyd.framework.dat.core.exception;

/**
 * 当没有 找到 JobTracker 节点的时候抛出这个异常
 */
public class TaskDispatcherNotFoundException extends Exception{

	private static final long serialVersionUID = -7804693020495753429L;

	public TaskDispatcherNotFoundException() {
    }

    public TaskDispatcherNotFoundException(String message) {
        super(message);
    }

    public TaskDispatcherNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TaskDispatcherNotFoundException(Throwable cause) {
        super(cause);
    }

}
