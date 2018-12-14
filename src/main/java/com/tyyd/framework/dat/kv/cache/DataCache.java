package com.tyyd.framework.dat.kv.cache;

/**
 * @author   on 12/18/15.
 */
public interface DataCache<K, V> {

    void put(K key, V value);

    V get(K key);

    V remove(K key);

    int size();

    void clear();
}
