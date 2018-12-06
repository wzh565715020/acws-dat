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

        String jobId = request.getParam("jobId");
        if (StringUtils.isEmpty(jobId)) {
            return HttpCmdResponse.newResponse(false, "jobId can't be empty");
        }

        if (!appContext.getRunnerPool().getRunningJobManager().running(jobId)) {
            return HttpCmdResponse.newResponse(false, "jobId dose not running in this TaskTracker now");
        }

        appContext.getRunnerPool().getRunningJobManager().terminateJob(jobId);

        return HttpCmdResponse.newResponse(true, "Execute terminate Command success");
    }
}
