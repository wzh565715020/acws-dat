package com.tyyd.framework.dat.kv.serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

/**
 *   12/5/15.
 */
public interface StoreSerializer {

    void serialize(Object value, OutputStream out) throws IOException;

    <T> T deserialize(InputStream in, Type type) throws IOException;
}
