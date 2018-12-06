package com.tyyd.framework.dat.taskexecuter;

import com.tyyd.framework.dat.core.domain.Action;

public class Result {

    private Action action;

    private String msg;

    public Result() {
    }

    public Result(Action action, String msg) {
        this.action = action;
        this.msg = msg;
    }

    public Result(Action action) {
        this.action = action;
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
}
