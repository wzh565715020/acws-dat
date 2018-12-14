package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.remoting.annotation.NotNull;

import java.util.List;

/**
 *         任务传递信息
 */
public class TaskSubmitRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 7229438891247265777L;
	
	@NotNull
    private List<Task> tasks;

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

}
