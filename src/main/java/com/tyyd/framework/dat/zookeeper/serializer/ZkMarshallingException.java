package com.tyyd.framework.dat.zookeeper.serializer;

/**
 * @author   on 5/17/15.
 */
public class ZkMarshallingException extends RuntimeException {

	private static final long serialVersionUID = 2489236644437409454L;

	public ZkMarshallingException() {
        super();
    }

    public ZkMarshallingException(String message) {
        super(message);
    }

    public ZkMarshallingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ZkMarshallingException(Throwable cause) {
        super(cause);
    }

}
