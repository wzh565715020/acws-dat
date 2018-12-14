package com.tyyd.framework.dat.remoting;

/**
 * @author   on 11/3/15.
 */
public interface Future {

    boolean isSuccess();

    Throwable cause();

}
