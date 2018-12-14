package com.tyyd.framework.dat.zookeeper.serializer;

/**
 * @author   on 5/17/15.
 */
public interface ZkSerializer {

    public byte[] serialize(Object data) throws ZkMarshallingException;

    public Object deserialize(byte[] bytes) throws ZkMarshallingException;
}
