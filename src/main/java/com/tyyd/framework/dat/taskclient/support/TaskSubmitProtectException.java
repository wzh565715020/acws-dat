package com.tyyd.framework.dat.taskclient.support;

import com.tyyd.framework.dat.core.exception.JobSubmitException;

public class TaskSubmitProtectException extends JobSubmitException {

	private static final long serialVersionUID = -5502779460920973581L;
	int concurrentSize;

    public TaskSubmitProtectException(int concurrentSize) {
        super();
        this.concurrentSize = concurrentSize;
    }

    public TaskSubmitProtectException(int concurrentSize, String message) {
        super(message);
        this.concurrentSize = concurrentSize;
    }

    public TaskSubmitProtectException(int concurrentSize, String message, Throwable cause) {
        super(message, cause);
        this.concurrentSize = concurrentSize;
    }

    public TaskSubmitProtectException(int concurrentSize, Throwable cause) {
        super(cause);
        this.concurrentSize = concurrentSize;
    }
}
