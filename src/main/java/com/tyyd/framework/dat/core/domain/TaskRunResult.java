package com.tyyd.framework.dat.core.domain;

import com.tyyd.framework.dat.core.json.JSON;

import java.io.Serializable;

/**
 * TaskTracker 任务执行结果
 */
public class TaskRunResult implements Serializable{

	private static final long serialVersionUID = 8622758290605000897L;

	private TaskMeta taskMeta;

    private Action action;

    private String msg;
    // 任务完成时间
    private Long time;
    
    public TaskMeta getTaskMeta() {
        return taskMeta;
    }

    public void setTaskMeta(TaskMeta taskMeta) {
        this.taskMeta = taskMeta;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }


	@Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
