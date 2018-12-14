package com.tyyd.framework.dat.kv;

import com.tyyd.framework.dat.kv.iterator.DBIterator;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;

/**
 * @author   on 12/13/15.
 */
public interface DB<K, V> {

    Logger LOGGER = LoggerFactory.getLogger(DB.class);

    void init() throws DBException;

    int size();

    boolean containsKey(K key);

    V get(K key);

    void put(K key, V value);

    void remove(K key);

    DBIterator<Entry<K, V>> iterator();

    void close();

}
