package com.tyyd.framework.dat.jvmmonitor;

public interface ReferenceCount {

    /**
     * 增加引用数量
     */
    long incrementAndGet();

    /**
     * 减少引用数量
     */
    long decrementAndGet();

    /**
     * 获取当前的引用数量
     */
    long getCurRefCount();
}
