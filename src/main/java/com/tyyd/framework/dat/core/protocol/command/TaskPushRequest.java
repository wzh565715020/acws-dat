package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.remoting.annotation.NotNull;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/14.
 */
public class TaskPushRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 2986743693237022215L;
	
	@NotNull
    private TaskMeta jobMeta;

    public TaskMeta getJobMeta() {
        return jobMeta;
    }

    public void setJobMeta(TaskMeta jobMeta) {
        this.jobMeta = jobMeta;
    }
}
