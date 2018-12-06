package com.tyyd.framework.dat.taskexecuter.task;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

public class TaskRunnerHolder {

    private static final Map<String, TaskRunner> JOB_RUNNER_MAP = new ConcurrentHashMap<String, TaskRunner>();

    static void add(String shardValue, TaskRunner jobRunner) {
        JOB_RUNNER_MAP.put(shardValue, jobRunner);
    }

    public static TaskRunner getJobRunner(String shardValue) {
        return JOB_RUNNER_MAP.get(shardValue);
    }
}
