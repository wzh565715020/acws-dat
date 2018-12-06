package com.tyyd.framework.dat.taskexecuter.logger;

public abstract class BizLoggerAdapter implements BizLogger {

    public abstract void setId(String jobId, String taskId);

    public abstract void removeId();

}
