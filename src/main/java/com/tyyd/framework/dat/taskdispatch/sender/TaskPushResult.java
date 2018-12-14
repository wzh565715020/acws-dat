package com.tyyd.framework.dat.taskdispatch.sender;

public enum TaskPushResult {
    NO_TASK, // 没有任务可执行
    SUCCESS, //推送成功
    FAILED,      //推送失败
    SENT_ERROR,
    NO_POOL
}
