package com.tyyd.framework.dat.remoting.serialize;

import com.tyyd.framework.dat.core.commons.io.UnsafeByteArrayInputStream;
import com.tyyd.framework.dat.core.commons.io.UnsafeByteArrayOutputStream;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class JavaSerializable implements RemotingSerializable {

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public byte[] serialize(Object obj) throws Exception {

        UnsafeByteArrayOutputStream bos = new UnsafeByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(obj);
            oos.flush();
            return bos.toByteArray();
        } finally {
            oos.close();
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public <T> T deserialize(byte[] data, Class<T> clazz) throws Exception {

        UnsafeByteArrayInputStream bin = new UnsafeByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bin);

        try {
            Object obj = ois.readObject();
            return (T) obj;
        } finally {
            ois.close();
        }
    }

}
