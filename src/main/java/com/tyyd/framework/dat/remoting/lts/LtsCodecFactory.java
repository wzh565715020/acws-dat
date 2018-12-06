package com.tyyd.framework.dat.remoting.lts;

import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.nio.channel.NioChannel;
import com.tyyd.framework.dat.nio.codec.Decoder;
import com.tyyd.framework.dat.nio.codec.Encoder;
import com.tyyd.framework.dat.nio.codec.FrameDecoder;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.codec.Codec;
import com.tyyd.framework.dat.remoting.common.RemotingHelper;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;

import java.nio.ByteBuffer;

/**
 * @author Robert HG (254963746@qq.com) on 2/8/16.
 */
public class LtsCodecFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    private Codec codec;

    public LtsCodecFactory(Codec codec) {
        this.codec = codec;
    }

    public class NioEncoder implements Encoder {

        @Override
        public ByteBuffer encode(NioChannel channel, Object msg) {

            if (msg == null) {
                LOGGER.error("Message is null");
                return null;
            }

            if (msg instanceof RemotingCommand) {
                try {
                    return codec.encode((RemotingCommand) msg);
                } catch (Exception e) {
                    Channel c = new LtsChannel(channel);
                    LOGGER.error("encode exception, addr={}, remotingCommand={}", RemotingHelper.parseChannelRemoteAddr(c), ((RemotingCommand) msg).toString(), e);
                    RemotingHelper.closeChannel(c);
                }
            } else {
                LOGGER.error("Message is instance of " + RemotingCommand.class.getName());
            }
            return null;
        }
    }

    public class NioDecoder extends FrameDecoder {
        @Override
        protected Object decode(NioChannel ch, byte[] frame) throws Exception {
            ByteBuffer byteBuffer = ByteBuffer.wrap(frame);
            try {
                return codec.decode(byteBuffer);
            } catch (Exception e) {
                Channel channel = new LtsChannel(ch);
                LOGGER.error("decode exception, {}", RemotingHelper.parseChannelRemoteAddr(channel), e);
                RemotingHelper.closeChannel(channel);
                throw e;
            }
        }
    }

    public Encoder getEncoder() {
        return new NioEncoder();
    }

    public Decoder getDecoder() {
        return new NioDecoder();
    }

}
