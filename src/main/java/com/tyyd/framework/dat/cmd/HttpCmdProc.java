package com.tyyd.framework.dat.cmd;

/**
 * Cmd 处理器
 * @author   on 10/26/15.
 */
public interface HttpCmdProc {

    String nodeIdentity();

    String getCommand();

    HttpCmdResponse execute(HttpCmdRequest request) throws Exception;

}
