package com.tyyd.framework.dat.remoting.netty;

import com.tyyd.framework.dat.remoting.ChannelHandler;
import com.tyyd.framework.dat.remoting.ChannelHandlerListener;
import com.tyyd.framework.dat.remoting.Future;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class NettyChannelHandler implements ChannelHandler {

    private ChannelFuture channelFuture;

    public NettyChannelHandler(ChannelFuture channelFuture) {
        this.channelFuture = channelFuture;
    }

    @Override
    public ChannelHandler addListener(final ChannelHandlerListener listener) {

        channelFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(final ChannelFuture future) throws Exception {
                listener.operationComplete(new Future() {
                    @Override
                    public boolean isSuccess() {
                        return future.isSuccess();
                    }

                    @Override
                    public Throwable cause() {
                        return future.cause();
                    }
                });
            }
        });

        return this;
    }
}
