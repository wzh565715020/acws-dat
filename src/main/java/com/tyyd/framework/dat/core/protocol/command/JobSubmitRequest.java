package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.remoting.annotation.NotNull;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 7/24/14.
 *         任务传递信息
 */
public class JobSubmitRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 7229438891247265777L;
	
	@NotNull
    private List<Task> jobs;

    public List<Task> getJobs() {
        return jobs;
    }

    public void setJobs(List<Task> jobs) {
        this.jobs = jobs;
    }

}
