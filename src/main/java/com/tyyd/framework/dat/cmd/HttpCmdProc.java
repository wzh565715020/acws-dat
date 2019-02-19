package com.tyyd.framework.dat.cmd;

/**
 * Cmd 处理器
 */
public interface HttpCmdProc {

    String nodeIdentity();

    String getCommand();

    HttpCmdResponse execute(HttpCmdRequest request) throws Exception;

}
