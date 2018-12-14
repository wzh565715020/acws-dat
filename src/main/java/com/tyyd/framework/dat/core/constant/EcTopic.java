package com.tyyd.framework.dat.core.constant;

/**
 * @author   on 5/11/15.
 */
public interface EcTopic {

    // 工作线程变化
    String WORK_THREAD_CHANGE = "WORK_THREAD_CHANGE";
    // 节点启用
    String NODE_ENABLE = "NODE_ENABLE";
    // 节点禁用
    String NODE_DISABLE = "NODE_DISABLE";

    // 没有可用的TASK_EXECUTER了
    String NO_TASK_EXECUTER_AVAILABLE = "NO_TASK_EXECUTER_AVAILABLE";
    // 有可用的TASK_EXECUTER了
    String TASK_EXECUTER_AVAILABLE = "TASK_EXECUTER_AVAILABLE";
    // master 节点改变了
    String MASTER_CHANGED = "MASTER_CHANGED";

    String NODE_ADD = "NODE_ADD";

    String NODE_REMOVE = "NODE_REMOVE";

    // 注册中心可用
    String REGISTRY_AVAILABLE = "REGISTRY_AVAILABLE";
    // 注册中心不可用
    String REGISTRY_UN_AVAILABLE = "REGISTRY_UN_AVAILABLE";

    String NODE_SHUT_DOWN = "NODE_SHUT_DOWN";

}
