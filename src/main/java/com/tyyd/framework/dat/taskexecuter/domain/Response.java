package com.tyyd.framework.dat.taskexecuter.domain;

import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.TaskMeta;

public class Response {

    private Action action;

    private String msg;

    private TaskMeta taskMeta;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

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

}
