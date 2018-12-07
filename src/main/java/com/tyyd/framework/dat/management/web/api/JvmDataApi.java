package com.tyyd.framework.dat.management.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyyd.framework.dat.cmd.DefaultHttpCmd;
import com.tyyd.framework.dat.cmd.HttpCmd;
import com.tyyd.framework.dat.cmd.HttpCmdClient;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cmd.HttpCmdNames;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.support.I18nManager;
import com.tyyd.framework.dat.management.web.AbstractMVC;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;

import java.util.Collections;

@RestController
@RequestMapping("/jvm")
public class JvmDataApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("node-jvm-info-get")
    public RestfulResponse getNodeList(String identity) {

        RestfulResponse restfulResponse = new RestfulResponse();

        Node node = appContext.getNodeMemCacheAccess().getNodeByIdentity(identity);

        if (node == null) {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg(I18nManager.getMessage("node.dose.not.alive"));
            return restfulResponse;
        }

        HttpCmd cmd = new DefaultHttpCmd();
        cmd.setCommand(HttpCmdNames.HTTP_CMD_JVM_INFO_GET);
        cmd.setNodeIdentity(identity);

        HttpCmdResponse response = HttpCmdClient.doGet(node.getIp(), node.getHttpCmdPort(), cmd);
        if (response.isSuccess()) {
            restfulResponse.setSuccess(true);
            restfulResponse.setResults(1);
            restfulResponse.setRows(Collections.singletonList(response.getObj()));
        } else {
            restfulResponse.setSuccess(false);
            restfulResponse.setMsg(response.getMsg());
        }

        return restfulResponse;
    }

}
