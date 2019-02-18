package com.tyyd.framework.dat.remoting.codec;

import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;

import java.nio.ByteBuffer;

public interface Codec {

    RemotingCommand decode(final ByteBuffer byteBuffer) throws Exception;

    ByteBuffer encode(final RemotingCommand remotingCommand) throws Exception;

}
