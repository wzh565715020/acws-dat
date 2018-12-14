package com.tyyd.framework.dat.kv.iterator;

/**
 * @author   on 12/13/15.
 */
public interface DBIterator<V> {

    boolean hasNext();

    V next();

}