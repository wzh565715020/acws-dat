package com.tyyd.framework.dat.core.failstore.ltsdb;

import com.tyyd.framework.dat.core.commons.file.FileUtils;
import com.tyyd.framework.dat.core.domain.Pair;
import com.tyyd.framework.dat.core.failstore.AbstractFailStore;
import com.tyyd.framework.dat.core.failstore.FailStoreException;
import com.tyyd.framework.dat.kv.DB;
import com.tyyd.framework.dat.kv.DBBuilder;
import com.tyyd.framework.dat.kv.Entry;
import com.tyyd.framework.dat.kv.iterator.DBIterator;
import com.tyyd.framework.dat.core.json.JSON;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 12/19/15.
 */
public class LtsdbFailStore extends AbstractFailStore {

    public static final String name = "ltsdb";

    private DB<String, String> db;

    public LtsdbFailStore(File dbPath, boolean needLock) {
        super(dbPath, needLock);
    }

    @Override
    protected void init() throws FailStoreException {
        try {
            db = new DBBuilder<String, String>()
                    .setPath(dbPath)
                    .create();
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void open() throws FailStoreException {
        try {
            db.init();
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void put(String key, Object value) throws FailStoreException {
        try {
            String valueString = JSON.toJSONString(value);
            db.put(key, valueString);
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(String key) throws FailStoreException {
        try {
            db.remove(key);
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public void delete(List<String> keys) throws FailStoreException {
        if (keys == null || keys.size() == 0) {
            return;
        }
        try {
            for (String key : keys) {
                db.remove(key);
            }
        } catch (Exception e) {
            throw new FailStoreException(e);
        }
    }

    @Override
    public <T> List<Pair<String, T>> fetchTop(int size, Type type) throws FailStoreException {
        List<Pair<String, T>> list = new ArrayList<Pair<String, T>>(size);
        if (db.size() == 0) {
            return list;
        }

        DBIterator<Entry<String, String>> iterator = db.iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            String key = entry.getKey();
            T value = JSON.parse(entry.getValue(), type);
            Pair<String, T> pair = new Pair<String, T>(key, value);
            list.add(pair);
            if (list.size() >= size) {
                break;
            }
        }
        return list;
    }

    @Override
    public void close() throws FailStoreException {
        db.close();
    }

    @Override
    public void destroy() throws FailStoreException {
        try {
            close();
        } catch (Exception e) {
            throw new FailStoreException(e);
        } finally {
            FileUtils.delete(dbPath);
        }
    }
}
