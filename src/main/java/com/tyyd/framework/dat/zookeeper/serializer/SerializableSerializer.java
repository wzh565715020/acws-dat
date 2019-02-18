package com.tyyd.framework.dat.zookeeper.serializer;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.tyyd.framework.dat.core.commons.io.UnsafeByteArrayInputStream;
import com.tyyd.framework.dat.core.commons.io.UnsafeByteArrayOutputStream;


public class SerializableSerializer implements ZkSerializer {
    @Override
    public byte[] serialize(Object serializable) throws ZkMarshallingException {
        try {
            if (serializable == null) {
                return null;
            }
            UnsafeByteArrayOutputStream byteArrayOS = new UnsafeByteArrayOutputStream();
            ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
            stream.writeObject(serializable);
            stream.close();
            return byteArrayOS.toByteArray();
        } catch (IOException e) {
            throw new ZkMarshallingException(e);
        }
    }

    @SuppressWarnings("resource")
	@Override
    public Object deserialize(byte[] bytes) throws ZkMarshallingException {
        if (bytes == null) {
            return null;
        }
        try {
            return new ObjectInputStream(new UnsafeByteArrayInputStream(bytes)).readObject();
        } catch (ClassNotFoundException e) {
            throw new ZkMarshallingException("Unable to find object class.", e);
        } catch (IOException e) {
            throw new ZkMarshallingException(e);
        }
    }
}
