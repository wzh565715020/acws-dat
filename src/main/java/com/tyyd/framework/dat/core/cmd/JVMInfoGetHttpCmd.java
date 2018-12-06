package com.tyyd.framework.dat.core.cmd;

import com.tyyd.framework.dat.cmd.HttpCmdProc;
import com.tyyd.framework.dat.cmd.HttpCmdRequest;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.jvmmonitor.JVMCollector;

import java.util.Map;

/**
 * 主要用于获取节点的JVM信息
 *
 * @author Robert HG (254963746@qq.com) on 3/10/16.
 */
public class JVMInfoGetHttpCmd implements HttpCmdProc {

    private Config config;

    public JVMInfoGetHttpCmd(Config config) {
        this.config = config;
    }

    @Override
    public String nodeIdentity() {
        return config.getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_JVM_INFO_GET;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        Map<String, Object> jvmInfo = JVMCollector.getJVMInfo();

        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(true);
        response.setObj(JSON.toJSONString(jvmInfo));

        return response;
    }

}
