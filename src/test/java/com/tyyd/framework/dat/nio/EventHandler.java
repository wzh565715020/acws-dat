package com.tyyd.framework.dat.nio;

import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.nio.channel.NioChannel;
import com.tyyd.framework.dat.nio.handler.NioHandler;
import com.tyyd.framework.dat.nio.idle.IdleState;

/**
 * @author Robert HG (254963746@qq.com) on 2/3/16.
 */
public class EventHandler implements NioHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);

    @Override
    public void exceptionCaught(NioChannel channel, Exception cause) {
        LOGGER.error("exceptionCaught - " + channel.remoteAddress(), cause);
    }

    @Override
    public void messageReceived(NioChannel channel, Object msg) throws Exception{
        LOGGER.info("messageReceived : " + channel.remoteAddress() + "  " + JSON.toJSONString(msg));
    }

    @Override
    public void channelConnected(NioChannel channel) {
        LOGGER.info("channelConnected - " + channel.remoteAddress());
    }

    @Override
    public void channelIdle(NioChannel channel, IdleState state) {

    }
}
