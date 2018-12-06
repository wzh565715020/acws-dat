package com.tyyd.framework.dat.taskexecuter.expcetion;

/**
 * 没有可用的线程
 */
public class NoAvailableTaskRunnerException extends Exception{

	private static final long serialVersionUID = 5317008601154858522L;

	public NoAvailableTaskRunnerException() {
        super();
    }

    public NoAvailableTaskRunnerException(String message) {
        super(message);
    }

    public NoAvailableTaskRunnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoAvailableTaskRunnerException(Throwable cause) {
        super(cause);
    }

}
