package com.tyyd.framework.dat.taskexecuter.cmd;

import com.tyyd.framework.dat.cmd.HttpCmdProc;
import com.tyyd.framework.dat.cmd.HttpCmdRequest;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.cmd.HttpCmdNames;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;

/**
 * 用于中断某个Job
 */
public class TaskTerminateCmd implements HttpCmdProc {

    private TaskExecuterAppContext appContext;

    public TaskTerminateCmd(TaskExecuterAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_JOB_TERMINATE;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String id = request.getParam("id");
        if (StringUtils.isEmpty(id)) {
            return HttpCmdResponse.newResponse(false, "id can't be empty");
        }

        if (!appContext.getRunnerPool().getRunningTaskManager().running(id)) {
            return HttpCmdResponse.newResponse(false, "id dose not running in this TaskTracker now");
        }

        appContext.getRunnerPool().getRunningTaskManager().terminateJob(id);

        return HttpCmdResponse.newResponse(true, "Execute terminate Command success");
    }
}
