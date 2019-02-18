package com.tyyd.framework.dat.remoting.serialize;


import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

@SPI(key = SpiExtensionKey.REMOTING_SERIALIZABLE_DFT, dftValue = "fastjson")
public interface RemotingSerializable {

    int getId();

    byte[] serialize(final Object obj) throws Exception;

    <T> T deserialize(final byte[] data, Class<T> clazz) throws Exception;
}
