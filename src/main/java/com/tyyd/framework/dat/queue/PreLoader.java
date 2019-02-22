package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.TaskPo;

public interface PreLoader {

	public TaskPo take();

	public boolean lockTask(String id, String taskExecuterIdentity,Long triggerTime);

	public void stop();
	
	public String getPoolId();
}
