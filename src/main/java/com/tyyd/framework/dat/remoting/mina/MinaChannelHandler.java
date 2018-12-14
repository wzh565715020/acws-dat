package com.tyyd.framework.dat.remoting.mina;

import com.tyyd.framework.dat.remoting.ChannelHandler;
import com.tyyd.framework.dat.remoting.ChannelHandlerListener;
import com.tyyd.framework.dat.remoting.Future;
import org.apache.mina.core.future.*;

/**
 * @author   on 11/4/15.
 */
public class MinaChannelHandler implements ChannelHandler {

    private IoFuture ioFuture;

    public MinaChannelHandler(IoFuture ioFuture) {
        this.ioFuture = ioFuture;
    }

    @Override
    public ChannelHandler addListener(final ChannelHandlerListener listener) {

        ioFuture.addListener(new IoFutureListener<IoFuture>() {
            @Override
            public void operationComplete(final IoFuture future) {
                try {
                    listener.operationComplete(new Future() {
                        @Override
                        public boolean isSuccess() {
                            if (ioFuture instanceof WriteFuture) {
                                return ((WriteFuture) future).isWritten();
                            } else if (ioFuture instanceof ConnectFuture) {
                                return ((ConnectFuture) future).isConnected();
                            } else if (ioFuture instanceof CloseFuture) {
                                return ((CloseFuture) ioFuture).isClosed();
                            }
                            return future.isDone();
                        }

                        @Override
                        public Throwable cause() {
                            return null;
                        }
                    });
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        return this;
    }
}
