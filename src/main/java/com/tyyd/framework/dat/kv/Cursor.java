package com.tyyd.framework.dat.kv;

/**
 * @author   on 12/19/15.
 */
public interface Cursor<V> {

    boolean hasNext();

    V next();
}
