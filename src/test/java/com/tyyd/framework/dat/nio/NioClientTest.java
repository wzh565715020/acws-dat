package com.tyyd.framework.dat.nio;

import com.tyyd.framework.dat.nio.channel.ChannelInitializer;
import com.tyyd.framework.dat.nio.channel.NioChannel;
import com.tyyd.framework.dat.nio.codec.Decoder;
import com.tyyd.framework.dat.nio.codec.Encoder;
import com.tyyd.framework.dat.nio.config.NioClientConfig;
import com.tyyd.framework.dat.nio.handler.Futures;
import com.tyyd.framework.dat.nio.handler.IoFutureListener;
import com.tyyd.framework.dat.remoting.Future;

import java.net.InetSocketAddress;

/**
 * @author Robert HG (254963746@qq.com) on 2/3/16.
 */
public class NioClientTest {

    public static void main(String[] args) {

        NioClientConfig clientConfig = new NioClientConfig();

        NioClient client = new NioClient(clientConfig, new EventHandler(), new ChannelInitializer() {
            @Override
            protected Decoder getDecoder() {
                return CodecFactory.getDecoder();
            }

            @Override
            protected Encoder getEncoder() {
                return CodecFactory.getEncoder();
            }
        });
        Futures.ConnectFuture connectFuture = client.connect(new InetSocketAddress("127.0.0.1", 8221));

        NioChannel channel = connectFuture.channel();

        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        RemotingMsg msg = new RemotingMsg();
        msg.setName("fdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasjfdasfhasfjlasj");
        msg.setB(true);
        msg.setType(1);

        while (true) {

            try {
                Thread.sleep(1000L);
                Futures.WriteFuture writeFuture = channel.writeAndFlush(msg);
                writeFuture.addListener(new IoFutureListener() {
                    @Override
                    public void operationComplete(Future future) throws Exception {
                        System.out.print(future.isSuccess() + "  ");
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}