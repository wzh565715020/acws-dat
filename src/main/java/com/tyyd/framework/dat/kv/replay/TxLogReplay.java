package com.tyyd.framework.dat.kv.replay;

import com.tyyd.framework.dat.kv.*;
import com.tyyd.framework.dat.kv.cache.DataCache;
import com.tyyd.framework.dat.kv.data.DataAppendResult;
import com.tyyd.framework.dat.kv.data.DataBlockEngine;
import com.tyyd.framework.dat.kv.index.Index;
import com.tyyd.framework.dat.kv.index.IndexItem;
import com.tyyd.framework.dat.kv.txlog.StoreTxLogCursorEntry;
import com.tyyd.framework.dat.kv.txlog.StoreTxLogEngine;
import com.tyyd.framework.dat.kv.txlog.StoreTxLogEntry;
import com.tyyd.framework.dat.kv.txlog.StoreTxLogPosition;
import com.tyyd.framework.dat.core.logger.Logger;

/**
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public class TxLogReplay<K, V> {

    private static final Logger LOGGER = DB.LOGGER;
    private StoreTxLogEngine<K, V> storeTxLogEngine;
    private DataBlockEngine<K, V> dataBlockEngine;
    private Index<K, V> index;
    private DataCache<K, V> dataCache;

    public TxLogReplay(StoreTxLogEngine<K, V> storeTxLogEngine, DataBlockEngine<K, V> dataBlockEngine, Index<K, V> index, DataCache<K, V> dataCache) {
        this.storeTxLogEngine = storeTxLogEngine;
        this.dataBlockEngine = dataBlockEngine;
        this.index = index;
        this.dataCache = dataCache;
    }

    public void replay(StoreTxLogPosition startPosition) {

        LOGGER.info("start to replay txLog ...");

        Cursor<StoreTxLogCursorEntry<K, V>> cursor = storeTxLogEngine.cursor(startPosition);

        int count = 0;

        while (cursor.hasNext()) {
            StoreTxLogCursorEntry<K, V> storeTxLogCursorEntry = cursor.next();

            StoreTxLogEntry<K, V> storeTxLogEntry = storeTxLogCursorEntry.getStoreTxLogEntry();

            Operation op = storeTxLogEntry.getOp();

            StoreTxLogPosition position = storeTxLogCursorEntry.getPosition();

            K key = storeTxLogEntry.getKey();
            V value = storeTxLogEntry.getValue();

            if (op == Operation.PUT) {
                // 1. 写Data
                DataAppendResult dataAppendResult = dataBlockEngine.append(position, key, value);
                // 2. 写Index
                index.putIndexItem(position, key, DBImpl.convertToIndex(key, dataAppendResult));
                // 3. 写缓存
                dataCache.put(key, value);

            } else if (op == Operation.REMOVE) {
                // 1. 移除Index
                IndexItem<K> indexItem = index.removeIndexItem(position, key);
                if (indexItem != null) {
                    // 2. 移除Data
                    dataBlockEngine.remove(position, indexItem);
                }
                // 2. 移除缓存
                dataCache.remove(key);
            } else {
                throw new DBException("error op=" + op);
            }

            count++;
        }

        LOGGER.info("replay txLog complete, txLog size:" + count);

    }

}
