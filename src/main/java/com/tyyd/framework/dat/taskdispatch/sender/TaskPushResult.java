package com.tyyd.framework.dat.taskdispatch.sender;

public enum TaskPushResult {
    NO_JOB, // 没有任务可执行
    SUCCESS, //推送成功
    FAILED,      //推送失败
    SENT_ERROR
}
