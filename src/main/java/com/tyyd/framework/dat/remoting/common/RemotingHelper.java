package com.tyyd.framework.dat.remoting.common;

import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.ChannelHandlerListener;
import com.tyyd.framework.dat.remoting.Future;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * 通信层一些辅助方法
 */
public class RemotingHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemotingHelper.RemotingLogName);

    public static final String RemotingLogName = "LtsRemoting";

    /**
     * IP:PORT
     */
    public static SocketAddress string2SocketAddress(final String addr) {
        String[] s = addr.split(":");
        return new InetSocketAddress(s[0], Integer.valueOf(s[1]));
    }

    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        final SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }

    public static void closeChannel(Channel channel) {
        final String addrRemote = RemotingHelper.parseChannelRemoteAddr(channel);
        channel.close().addListener(new ChannelHandlerListener() {
            @Override
            public void operationComplete(Future future) throws Exception {
                LOGGER.info("closeChannel: close the connection to remote address[{}] result: {}", addrRemote,
                        future.isSuccess());
            }
        });
    }
/*    public static io.netty.channel.Channel getChannel(String host,int port) {
    	EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    	Bootstrap bootstrap = new Bootstrap();
    	bootstrap.channel(NioSocketChannel.class);
    	bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
    	bootstrap.option(ChannelOption.TCP_NODELAY, true);
    	bootstrap.group(eventLoopGroup);
    	bootstrap.remoteAddress(host, port);
    	bootstrap.handler(new ChannelInitializer<SocketChannel>() {
    		@Override
    		protected void initChannel(SocketChannel socketChannel) throws Exception {
    			socketChannel.pipeline().addLast(new MessageDecoder(), new MessageEncoder(), new NettyClientHandler());
    		}
    	});
    	ChannelFuture future = bootstrap.connect(host, port).sync();
    	if (future.isSuccess()) {
    		return future.channel();
    	}
    	return null;
	}*/

}
