package com.tyyd.framework.dat.json;

/**
 * @author   on 12/28/15.
 */
public class JSONException extends RuntimeException {

    private static final long serialVersionUID = 0;

    public JSONException(final String message) {
        super(message);
    }

    public JSONException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public JSONException(final Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
