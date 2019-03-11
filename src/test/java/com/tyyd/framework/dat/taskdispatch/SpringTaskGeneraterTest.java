package com.tyyd.framework.dat.taskdispatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.domain.TaskExecType;
import com.tyyd.framework.dat.core.domain.TaskType;
import com.tyyd.framework.dat.taskdispatch.support.util.TaskReceiveUtil;

public class SpringTaskGeneraterTest {

	public static void main(String[] args) throws Exception {
		new ClassPathXmlApplicationContext("/spring/taskgenerater.xml");
		while (true) {
			for (int i = 1; i <= 6; i++) {
				Task task = new Task();
				task.setMaxRetryTimes(0);
				task.setMemo("");
				task.setParams("{pooId:" + String.valueOf(i) + "}");
				task.setPoolId(String.valueOf(i));
				task.setRepeatCount(0);
				task.setRepeatedCount(0);
				task.setRepeatInterval(0l);
				task.setSubmitNode("");
				task.setTaskClass("defaultAcwsTask");
				task.setTaskExecType(TaskExecType.IMMEDIATELY.getCode());
				task.setTaskName("pooId" + String.valueOf(i) + " task");
				task.setTaskType(TaskType.SINGLE.getCode());
				task.setTriggerTime(1l);
				TaskReceiveUtil.addToQueue(task);
			}
			Thread.currentThread().sleep(1000);
		}
	}

}
