package com.tyyd.framework.dat.remoting;

import java.util.EventListener;

/**
 * @author   on 11/3/15.
 */
public interface ChannelHandlerListener extends EventListener {

    void operationComplete(Future future) throws Exception;
}
