package com.tyyd.framework.dat.taskexecuter.task;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

public class TaskRunnerHolder {

    private static final Map<String, TaskRunner> TASK_RUNNER_MAP = new ConcurrentHashMap<String, TaskRunner>();

    static void add(String annotation, TaskRunner taskRunner) {
        TASK_RUNNER_MAP.put(annotation, taskRunner);
    }

    public static TaskRunner getTaskRunner(String annotation) {
        return TASK_RUNNER_MAP.get(annotation);
    }
}
