package com.tyyd.framework.dat.remoting.mina;

import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.remoting.AbstractRemoting;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.RemotingEvent;
import com.tyyd.framework.dat.remoting.RemotingEventType;
import com.tyyd.framework.dat.remoting.common.RemotingHelper;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class MinaHandler extends IoHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    private AbstractRemoting remoting;
    private String sideType;      // SERVER , CLIENT

    public MinaHandler(AbstractRemoting remoting) {
        this.remoting = remoting;
        if (remoting instanceof MinaRemotingClient) {
            sideType = "CLIENT";
        } else {
            sideType = "SERVER";
        }
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(new MinaChannel(session));
        LOGGER.info("{} : sessionCreated {}", sideType, remoteAddress);
        super.sessionCreated(session);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Channel channel = new MinaChannel(session);
        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
        LOGGER.info("{}: sessionOpened, the channel[{}]", sideType, remoteAddress);

        if (remoting.getChannelEventListener() != null) {
            remoting.putRemotingEvent(new RemotingEvent(RemotingEventType.CONNECT, remoteAddress, channel));
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        com.tyyd.framework.dat.remoting.Channel channel = new MinaChannel(session);

        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
        LOGGER.info("{}: sessionClosed, the channel[{}]", sideType, remoteAddress);

        if (remoting.getChannelEventListener() != null) {
            remoting.putRemotingEvent(new RemotingEvent(RemotingEventType.CLOSE, remoteAddress, channel));
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        com.tyyd.framework.dat.remoting.Channel channel = new MinaChannel(session);

        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);

        if (IdleStatus.BOTH_IDLE == status) {
            LOGGER.info("{}: IDLE [{}]", sideType, remoteAddress);
            RemotingHelper.closeChannel(channel);
        }

        if (remoting.getChannelEventListener() != null) {
            RemotingEventType remotingEventType = null;
            if (IdleStatus.BOTH_IDLE == status) {
                remotingEventType = RemotingEventType.ALL_IDLE;
            } else if (IdleStatus.READER_IDLE == status) {
                remotingEventType = RemotingEventType.READER_IDLE;
            } else if (IdleStatus.WRITER_IDLE == status) {
                remotingEventType = RemotingEventType.WRITER_IDLE;
            }
            remoting.putRemotingEvent(new RemotingEvent(remotingEventType,
                    remoteAddress, channel));
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        com.tyyd.framework.dat.remoting.Channel channel = new MinaChannel(session);

        final String remoteAddress = RemotingHelper.parseChannelRemoteAddr(channel);
        LOGGER.warn("{}: exceptionCaught {}. ", sideType, remoteAddress, cause);

        if (remoting.getChannelEventListener() != null) {
            remoting.putRemotingEvent(new RemotingEvent(RemotingEventType.EXCEPTION, remoteAddress, channel));
        }

        RemotingHelper.closeChannel(channel);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (message != null && message instanceof RemotingCommand) {
            remoting.processMessageReceived(new MinaChannel(session), (RemotingCommand) message);
        }
    }
}
