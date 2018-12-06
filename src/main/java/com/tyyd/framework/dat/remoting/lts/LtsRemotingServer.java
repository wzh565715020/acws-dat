package com.tyyd.framework.dat.remoting.lts;

import com.tyyd.framework.dat.nio.NioServer;
import com.tyyd.framework.dat.nio.channel.ChannelInitializer;
import com.tyyd.framework.dat.nio.codec.Decoder;
import com.tyyd.framework.dat.nio.codec.Encoder;
import com.tyyd.framework.dat.nio.config.NioServerConfig;
import com.tyyd.framework.dat.remoting.AbstractRemotingServer;
import com.tyyd.framework.dat.remoting.ChannelEventListener;
import com.tyyd.framework.dat.remoting.RemotingServerConfig;
import com.tyyd.framework.dat.remoting.exception.RemotingException;

import java.net.InetSocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsRemotingServer extends AbstractRemotingServer {

    private NioServer server;

    public LtsRemotingServer(RemotingServerConfig remotingServerConfig, ChannelEventListener channelEventListener) {
        super(remotingServerConfig, channelEventListener);
    }

    public LtsRemotingServer(RemotingServerConfig remotingServerConfig) {
        this(remotingServerConfig, null);
    }

    @Override
    protected void serverStart() throws RemotingException {
        NioServerConfig serverConfig = new NioServerConfig();
        serverConfig.setBacklog(65536);
        serverConfig.setReuseAddress(true);
        serverConfig.setTcpNoDelay(true);

        serverConfig.setIdleTimeBoth(remotingServerConfig.getServerChannelMaxIdleTimeSeconds());
        serverConfig.setIdleTimeWrite(remotingServerConfig.getWriterIdleTimeSeconds());
        serverConfig.setIdleTimeRead(remotingServerConfig.getReaderIdleTimeSeconds());

        final LtsCodecFactory codecFactory = new LtsCodecFactory(getCodec());

        server = new NioServer(serverConfig, new LtsEventHandler(this, "SERVER"), new ChannelInitializer() {
            @Override
            protected Decoder getDecoder() {
                return codecFactory.getDecoder();
            }

            @Override
            protected Encoder getEncoder() {
                return codecFactory.getEncoder();
            }
        });

        server.bind(new InetSocketAddress(this.remotingServerConfig.getListenPort()));
    }

    @Override
    protected void serverShutdown() throws RemotingException {
        server.shutdownGracefully();
    }

}
