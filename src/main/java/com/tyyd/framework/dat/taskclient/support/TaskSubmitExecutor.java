package com.tyyd.framework.dat.taskclient.support;


import java.util.List;

import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.exception.JobSubmitException;

public interface TaskSubmitExecutor<T> {

    T execute(List<Task> jobs) throws JobSubmitException;

}
