package com.tyyd.framework.dat.nio;

import com.tyyd.framework.dat.nio.channel.ChannelInitializer;
import com.tyyd.framework.dat.nio.codec.Decoder;
import com.tyyd.framework.dat.nio.codec.Encoder;
import com.tyyd.framework.dat.nio.config.NioClientConfig;
import com.tyyd.framework.dat.nio.handler.Futures;
import com.tyyd.framework.dat.nio.handler.NioHandler;
import com.tyyd.framework.dat.nio.processor.NioClientProcessor;

import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 1/30/16.
 */
public class NioClient {

    private NioClientProcessor processor;

    public NioClient(NioClientConfig clientConfig, NioHandler eventHandler, ChannelInitializer channelInitializer) {
        this.processor = new NioClientProcessor(clientConfig, eventHandler, channelInitializer);
    }

    public Futures.ConnectFuture connect(SocketAddress remoteAddress) {

        processor.start();

        return processor.connect(remoteAddress);
    }

    public void shutdownGracefully() {
        // TODO
    }
}
