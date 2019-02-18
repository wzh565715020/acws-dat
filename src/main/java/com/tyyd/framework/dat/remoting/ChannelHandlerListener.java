package com.tyyd.framework.dat.remoting;

import java.util.EventListener;

public interface ChannelHandlerListener extends EventListener {

    void operationComplete(Future future) throws Exception;
}
