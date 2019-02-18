package com.tyyd.framework.dat.remoting.codec;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.remoting.serialize.AdaptiveSerializable;
import com.tyyd.framework.dat.remoting.serialize.RemotingSerializable;

public abstract class AbstractCodec implements Codec {

    protected RemotingSerializable getRemotingSerializable(int serializableTypeId) {

        RemotingSerializable serializable = null;
        if (serializableTypeId > 0) {
            serializable = AdaptiveSerializable.getSerializableById(serializableTypeId);
            if (serializable == null) {
                throw new IllegalArgumentException("Can not support RemotingSerializable that serializableTypeId=" + serializableTypeId);
            }
        } else {
            serializable = ServiceLoader.load(RemotingSerializable.class, Constants.ADAPTIVE);
        }
        return serializable;
    }

}
