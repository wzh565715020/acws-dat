package com.tyyd.framework.dat.zookeeper;

public interface DataListener {

    void dataChange(String dataPath, Object data) throws Exception;

    void dataDeleted(String dataPath) throws Exception;
}
