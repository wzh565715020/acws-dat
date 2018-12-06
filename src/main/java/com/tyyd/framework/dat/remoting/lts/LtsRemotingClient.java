package com.tyyd.framework.dat.remoting.lts;

import com.tyyd.framework.dat.nio.NioClient;
import com.tyyd.framework.dat.nio.channel.ChannelInitializer;
import com.tyyd.framework.dat.nio.codec.Decoder;
import com.tyyd.framework.dat.nio.codec.Encoder;
import com.tyyd.framework.dat.nio.config.NioClientConfig;
import com.tyyd.framework.dat.nio.handler.Futures;
import com.tyyd.framework.dat.remoting.AbstractRemotingClient;
import com.tyyd.framework.dat.remoting.ChannelEventListener;
import com.tyyd.framework.dat.remoting.ChannelFuture;
import com.tyyd.framework.dat.remoting.RemotingClientConfig;
import com.tyyd.framework.dat.remoting.exception.RemotingException;

import java.net.SocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsRemotingClient extends AbstractRemotingClient {

    private NioClient client;

    public LtsRemotingClient(RemotingClientConfig remotingClientConfig, ChannelEventListener channelEventListener) {
        super(remotingClientConfig, channelEventListener);
    }

    public LtsRemotingClient(RemotingClientConfig remotingClientConfig) {
        this(remotingClientConfig, null);
    }

    @Override
    protected void clientStart() throws RemotingException {
        NioClientConfig clientConfig = new NioClientConfig();
        clientConfig.setTcpNoDelay(true);
        clientConfig.setIdleTimeBoth(remotingClientConfig.getClientChannelMaxIdleTimeSeconds());
        clientConfig.setIdleTimeRead(remotingClientConfig.getReaderIdleTimeSeconds());
        clientConfig.setIdleTimeWrite(remotingClientConfig.getWriterIdleTimeSeconds());

        final LtsCodecFactory codecFactory = new LtsCodecFactory(getCodec());

        this.client = new NioClient(clientConfig, new LtsEventHandler(this, "CLIENT"), new ChannelInitializer() {
            @Override
            protected Decoder getDecoder() {
                return codecFactory.getDecoder();
            }

            @Override
            protected Encoder getEncoder() {
                return codecFactory.getEncoder();
            }
        });
    }

    @Override
    protected void clientShutdown() {
        client.shutdownGracefully();
    }

    @Override
    protected ChannelFuture connect(SocketAddress socketAddress) {
        Futures.ConnectFuture connectFuture = client.connect(socketAddress);
        return new LtsChannelFuture(connectFuture);
    }

}
