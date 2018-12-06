package com.tyyd.framework.dat.taskexecuter.task;

import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

public class TaskExecuterDispatcher implements TaskRunner {

    private String shardField = "taskId";

    @Override
    public Result run(Job job) throws Throwable {

        String value;
        if (shardField.equals("taskId")) {
            value = job.getTaskId();
        } else {
            value = job.getParam(shardField);
        }

        TaskRunner jobRunner = null;
        if (StringUtils.isNotEmpty(value)) {
            jobRunner = TaskRunnerHolder.getJobRunner(value);
        }
        if (jobRunner == null) {
            jobRunner = TaskRunnerHolder.getJobRunner("_LTS_DEFAULT");

            if (jobRunner == null) {
                throw new TaskDispatchException("Can not find JobRunner by Shard Value : [" + value + "]");
            }
        }
        return jobRunner.run(job);
    }

    public void setShardField(String shardField) {
        if (StringUtils.isNotEmpty(shardField)) {
            this.shardField = shardField;
        }
    }

}
