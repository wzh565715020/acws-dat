package com.tyyd.framework.dat.biz.logger.domain;

public enum LogType {

    RECEIVE,         // 接受任务
    SENT,            // 任务发送 开始执行
    FINISHED,        // 任务执行完成
    RESEND,          // TaskTracker 重新发送的任务执行结果
    FIXED_DEAD,       // 修复死掉的任务
    BIZ,             // 业务日志
    DEL,             // 删除
    SUSPEND,        // 暂停
    RESUME        // 恢复
}
