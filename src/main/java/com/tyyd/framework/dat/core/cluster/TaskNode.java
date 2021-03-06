package com.tyyd.framework.dat.core.cluster;

/**
 * 节点接口
 */
public interface TaskNode {

    /**
     * 启动节点
     */
    public void start();

    /**
     * 停止节点
     */
    public void stop();

    /**
     * destroy
     */
    public void destroy();
}
