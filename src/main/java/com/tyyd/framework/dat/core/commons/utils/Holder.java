package com.tyyd.framework.dat.core.commons.utils;

/**
 * @author   on 5/18/15.
 */
public class Holder<T> {

    private volatile T value;

    public Holder(T value) {
        this.value = value;
    }

    public Holder(){
    }

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

}
