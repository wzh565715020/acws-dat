package com.tyyd.framework.dat.zookeeper;

/**
 * @author   on 7/8/14.
 */
public interface StateListener {

    int DISCONNECTED = 0;

    int CONNECTED = 1;

    int RECONNECTED = 2;

    void stateChanged(int connected);

}