package com.tyyd.framework.dat.remoting.lts;

import com.tyyd.framework.dat.nio.handler.IoFuture;
import com.tyyd.framework.dat.nio.handler.IoFutureListener;
import com.tyyd.framework.dat.remoting.ChannelHandler;
import com.tyyd.framework.dat.remoting.ChannelHandlerListener;
import com.tyyd.framework.dat.remoting.Future;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsChannelHandler implements ChannelHandler {

    private IoFuture future;

    public LtsChannelHandler(IoFuture future) {
        this.future = future;
    }

    @Override
    public ChannelHandler addListener(final ChannelHandlerListener listener) {
        future.addListener(new IoFutureListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                listener.operationComplete(future);
            }
        });
        return this;
    }
}
