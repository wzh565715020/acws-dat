package com.tyyd.framework.dat.taskdispatch.cmd;

import com.tyyd.framework.dat.cmd.HttpCmdProc;
import com.tyyd.framework.dat.cmd.HttpCmdRequest;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;

/**
 * 一些系统配置更改CMD
 */
public class SysConfigModifyHttpCmd implements HttpCmdProc {

    @Override
    public String nodeIdentity() {
        return null;
    }

    @Override
    public String getCommand() {
        return null;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {
        return null;
    }
}
