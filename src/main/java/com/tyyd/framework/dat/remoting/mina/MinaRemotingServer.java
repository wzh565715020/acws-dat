package com.tyyd.framework.dat.remoting.mina;

import com.tyyd.framework.dat.remoting.AbstractRemotingServer;
import com.tyyd.framework.dat.remoting.ChannelEventListener;
import com.tyyd.framework.dat.remoting.RemotingServerConfig;
import com.tyyd.framework.dat.remoting.exception.RemotingException;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IoSessionConfig;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.logging.MdcInjectionFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * @author   on 11/4/15.
 */
public class MinaRemotingServer extends AbstractRemotingServer {

    private IoAcceptor acceptor;
    private InetSocketAddress bindAddress;

    public MinaRemotingServer(RemotingServerConfig remotingServerConfig) {
        this(remotingServerConfig, null);
    }

    public MinaRemotingServer(RemotingServerConfig remotingServerConfig, ChannelEventListener channelEventListener) {
        super(remotingServerConfig, channelEventListener);
    }

    @Override
    protected void serverStart() throws RemotingException {

        acceptor = new NioSocketAcceptor(); //TCP Acceptor

        // acceptor.getFilterChain().addFirst("logging", new MinaLoggingFilter());
        acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new MinaCodecFactory(getCodec())));
        acceptor.getFilterChain().addLast("mdc", new MdcInjectionFilter());

        acceptor.setHandler(new MinaHandler(this));
        IoSessionConfig cfg = acceptor.getSessionConfig();
        cfg.setReaderIdleTime(remotingServerConfig.getReaderIdleTimeSeconds());
        cfg.setWriterIdleTime(remotingServerConfig.getWriterIdleTimeSeconds());
        cfg.setBothIdleTime(remotingServerConfig.getServerChannelMaxIdleTimeSeconds());

        bindAddress = new InetSocketAddress(remotingServerConfig.getListenPort());
        try {
            acceptor.bind(bindAddress);
        } catch (IOException e) {
            throw new RemotingException("Start Mina server error", e);
        }
    }

    @Override
    protected void serverShutdown() throws RemotingException{
        if (acceptor != null) {
            acceptor.unbind(bindAddress);
            acceptor.dispose();
        }
    }
}
