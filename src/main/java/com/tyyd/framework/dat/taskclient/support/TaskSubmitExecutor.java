package com.tyyd.framework.dat.taskclient.support;


import java.util.List;

import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.core.exception.JobSubmitException;

public interface TaskSubmitExecutor<T> {

    T execute(List<Job> jobs) throws JobSubmitException;

}
