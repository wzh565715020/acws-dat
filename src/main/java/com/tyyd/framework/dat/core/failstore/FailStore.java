package com.tyyd.framework.dat.core.failstore;

import com.tyyd.framework.dat.core.domain.Pair;

import java.lang.reflect.Type;
import java.util.List;

public interface FailStore {

    public String getPath();

    public void open() throws FailStoreException;

    public void put(String key, Object value) throws FailStoreException;

    public void delete(String key) throws FailStoreException;

    public void delete(List<String> keys) throws FailStoreException;

    public <T> List<Pair<String, T>> fetchTop(int size, Type type) throws FailStoreException;

    public void close() throws FailStoreException;

    public void destroy() throws FailStoreException;
}
