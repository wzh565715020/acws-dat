package com.tyyd.framework.dat.core.domain;

import com.tyyd.framework.dat.core.constant.Level;

import java.io.Serializable;

public class BizLog implements Serializable {

	private static final long serialVersionUID = -7770486329649514754L;

	private String taskId;

    private String id;

    private String msg;

    private Level level;

    private Long logTime;

    private String taskExecuteNode;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Long getLogTime() {
        return logTime;
    }

    public void setLogTime(Long logTime) {
        this.logTime = logTime;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTaskExecuteNode() {
		return taskExecuteNode;
	}

	public void setTaskExecuteNode(String taskExecuteNode) {
		this.taskExecuteNode = taskExecuteNode;
	}

}
