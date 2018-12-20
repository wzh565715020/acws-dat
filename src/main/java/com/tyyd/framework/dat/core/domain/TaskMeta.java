package com.tyyd.framework.dat.core.domain;

import com.tyyd.framework.dat.core.json.JSON;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TaskMeta implements Serializable {

    private static final long serialVersionUID = 1476984243004969158L;

    private String id;
    private Map<String, String> internalExtParams;

    private String taskExecuteNode;
    private Task task;

    public TaskMeta() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
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

    public String getTaskExecuteNode() {
		return taskExecuteNode;
	}

	public void setTaskExecuteNode(String taskExecuteNode) {
		this.taskExecuteNode = taskExecuteNode;
	}

	@Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
