package com.tyyd.framework.dat.taskdispatch.cmd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.cmd.HttpCmdProc;
import com.tyyd.framework.dat.cmd.HttpCmdRequest;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.cmd.HttpCmdNames;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 * 给JobTracker发送信号，加载任务
 *
 */
public class LoadTaskHttpCmd implements HttpCmdProc {

    private final Logger LOGGER = LoggerFactory.getLogger(LoadTaskHttpCmd.class);

    private TaskDispatcherAppContext appContext;

    public LoadTaskHttpCmd(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_LOAD_JOB;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String taskTrackerNodeGroup = request.getParam("nodeGroup");
        //appContext.getPreLoader().load(taskTrackerNodeGroup);

        LOGGER.info("load job succeed : nodeGroup={}", taskTrackerNodeGroup);

        return HttpCmdResponse.newResponse(true, "load job succeed");
    }
}
