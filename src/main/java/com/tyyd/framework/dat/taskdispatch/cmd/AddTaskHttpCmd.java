package com.tyyd.framework.dat.taskdispatch.cmd;


import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.cmd.HttpCmdProc;
import com.tyyd.framework.dat.cmd.HttpCmdRequest;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.cmd.HttpCmdNames;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.protocol.command.JobSubmitRequest;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

/**
 * 添加任务
 *
 */
public class AddTaskHttpCmd implements HttpCmdProc {

    private static final Logger LOGGER = LoggerFactory.getLogger(AddTaskHttpCmd.class);

    private TaskDispatcherAppContext appContext;

    public AddTaskHttpCmd(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_ADD_JOB;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        HttpCmdResponse response = new HttpCmdResponse();
        response.setSuccess(false);

        String jobJSON = request.getParam("job");
        if (StringUtils.isEmpty(jobJSON)) {
            response.setMsg("job can not be null");
            return response;
        }
        try {
            Job job = JSON.parse(jobJSON, Job.class);
            if (job == null) {
                response.setMsg("job can not be null");
                return response;
            }

            if (job.isNeedFeedback() && StringUtils.isEmpty(job.getSubmitNodeGroup())) {
                response.setMsg("if needFeedback, job.SubmitNodeGroup can not be null");
                return response;
            }

            job.checkField();

            JobSubmitRequest jobSubmitRequest = new JobSubmitRequest();
            jobSubmitRequest.setJobs(Collections.singletonList(job));
            appContext.getJobReceiver().receive(jobSubmitRequest);

            LOGGER.info("add job succeed, {}", job);

            response.setSuccess(true);

        } catch (Exception e) {
            LOGGER.error("add job error, message:", e);
            response.setMsg("add job error, message:" + e.getMessage());
        }
        return response;
    }

}
