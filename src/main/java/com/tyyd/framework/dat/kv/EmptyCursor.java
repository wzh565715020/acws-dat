package com.tyyd.framework.dat.kv;

/**
 * @author   on 12/19/15.
 */
public class EmptyCursor<V> implements Cursor<V> {
    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public V next() {
        return null;
    }
}
