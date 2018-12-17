package com.tyyd.framework.dat.zookeeper.serializer;

public interface ZkSerializer {

    public byte[] serialize(Object data) throws ZkMarshallingException;

    public Object deserialize(byte[] bytes) throws ZkMarshallingException;
}
