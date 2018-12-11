package com.tyyd.framework.dat.core.domain;

import com.tyyd.framework.dat.core.json.JSON;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TaskMeta implements Serializable {

    private static final long serialVersionUID = 1476984243004969158L;

    private String taskId;
    private Map<String, String> internalExtParams;

    private Task task;

    public TaskMeta() {
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Task getJob() {
        return task;
    }

    public void setJob(Task job) {
        this.task = job;
    }

    public Map<String, String> getInternalExtParams() {
        return internalExtParams;
    }

    public void setInternalExtParams(Map<String, String> internalExtParams) {
        this.internalExtParams = internalExtParams;
    }

    public String getInternalExtParam(String key) {
        if (internalExtParams == null) {
            return null;
        }
        return internalExtParams.get(key);
    }

    public void setInternalExtParam(String key, String value) {
        if (internalExtParams == null) {
            internalExtParams = new HashMap<String, String>();
        }
        internalExtParams.put(key, value);
    }

    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
