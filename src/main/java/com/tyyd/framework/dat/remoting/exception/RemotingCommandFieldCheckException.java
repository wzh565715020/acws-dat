package com.tyyd.framework.dat.remoting.exception;

public class RemotingCommandFieldCheckException extends Exception{

	private static final long serialVersionUID = -3040346783583325400L;

	public RemotingCommandFieldCheckException(String message) {
        super(message);
    }

    public RemotingCommandFieldCheckException(String message, Throwable cause) {
        super(message, cause);
    }
}
