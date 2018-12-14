package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.core.domain.TaskMeta;
import com.tyyd.framework.dat.remoting.annotation.NotNull;

public class TaskPushRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 2986743693237022215L;
	
	@NotNull
    private TaskMeta taskMeta;

    public TaskMeta getTaskMeta() {
        return taskMeta;
    }

    public void setTaskMeta(TaskMeta taskMeta) {
        this.taskMeta = taskMeta;
    }
}
