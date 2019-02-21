package com.tyyd.framework.dat.core.json;

public class JSONException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 560085675086688144L;

	public JSONException() {
        super();
    }

    public JSONException(String message) {
        super(message);
    }

    public JSONException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSONException(Throwable cause) {
        super(cause);
    }
}
