package com.tyyd.framework.dat.remoting.lts;

import com.tyyd.framework.dat.nio.handler.Futures;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.ChannelFuture;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsChannelFuture implements ChannelFuture {

    private Futures.ConnectFuture future;

    public LtsChannelFuture(Futures.ConnectFuture future) {
        this.future = future;
    }

    @Override
    public boolean isConnected() {
        return future.channel().isConnected();
    }

    @Override
    public Channel getChannel() {
        return new LtsChannel(future.channel());
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return future.awaitUninterruptibly(timeoutMillis);
    }

    @Override
    public boolean isDone() {
        return future.isDone();
    }

    @Override
    public Throwable cause() {
        return future.cause();
    }
}
