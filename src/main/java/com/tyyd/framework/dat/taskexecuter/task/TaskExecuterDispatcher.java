package com.tyyd.framework.dat.taskexecuter.task;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

public class TaskExecuterDispatcher implements TaskRunner {

    @Override
    public Result run(Task task) throws Throwable {

        String value = task.getTaskClass();

        TaskRunner taskRunner = null;
        if (StringUtils.isNotEmpty(value)) {
            taskRunner = TaskRunnerHolder.getTaskRunner(value);
        }
        if (taskRunner == null) {
            taskRunner = TaskRunnerHolder.getTaskRunner("_DAT_DEFAULT");

            if (taskRunner == null) {
                throw new TaskDispatchException("Can not find JobRunner by Shard Value : [" + value + "]");
            }
        }
        return taskRunner.run(task);
    }

}
