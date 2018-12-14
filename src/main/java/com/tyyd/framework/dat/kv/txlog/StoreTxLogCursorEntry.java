package com.tyyd.framework.dat.kv.txlog;

/**
 * @author   on 12/19/15.
 */
public class StoreTxLogCursorEntry<K, V> {

    private StoreTxLogEntry<K, V> storeTxLogEntry;

    private StoreTxLogPosition position;

    public StoreTxLogEntry<K, V> getStoreTxLogEntry() {
        return storeTxLogEntry;
    }

    public void setStoreTxLogEntry(StoreTxLogEntry<K, V> storeTxLogEntry) {
        this.storeTxLogEntry = storeTxLogEntry;
    }

    public StoreTxLogPosition getPosition() {
        return position;
    }

    public void setPosition(StoreTxLogPosition position) {
        this.position = position;
    }
}
