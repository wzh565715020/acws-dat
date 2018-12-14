package com.tyyd.framework.dat.json.deserializer;

import java.lang.reflect.Type;

/**
 * @author   on 12/30/15.
 */
public interface Deserializer {

    <T> T deserialize(Object object, Type type);

}
