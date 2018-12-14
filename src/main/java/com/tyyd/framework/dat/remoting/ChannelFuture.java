package com.tyyd.framework.dat.remoting;

/**
 * @author   on 11/4/15.
 */
public interface ChannelFuture {

    boolean isConnected();

    Channel getChannel();

    boolean awaitUninterruptibly(long timeoutMillis);

    boolean isDone();

    Throwable cause();
}
