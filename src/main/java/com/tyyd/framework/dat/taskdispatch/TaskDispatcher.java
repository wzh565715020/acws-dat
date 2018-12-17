package com.tyyd.framework.dat.taskdispatch;


import com.tyyd.framework.dat.biz.logger.SmartJobLogger;
import com.tyyd.framework.dat.core.cluster.AbstractClientNode;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.ec.injvm.InjvmEventCenter;
import com.tyyd.framework.dat.queue.TaskQueueFactory;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelManager;
import com.tyyd.framework.dat.taskdispatch.cmd.AddTaskHttpCmd;
import com.tyyd.framework.dat.taskdispatch.cmd.LoadTaskHttpCmd;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherNode;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;
import com.tyyd.framework.dat.taskdispatch.processor.RemotingDispatcher;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;
import com.tyyd.framework.dat.taskdispatch.support.TaskPushMachine;
import com.tyyd.framework.dat.taskdispatch.support.TaskReceiver;
import com.tyyd.framework.dat.taskdispatch.support.cluster.TaskExecuterManager;
import com.tyyd.framework.dat.taskdispatch.support.listener.TaskNodeChangeListener;
import com.tyyd.framework.dat.taskdispatch.support.listener.TaskDispatcherMasterChangeListener;

public class TaskDispatcher extends AbstractClientNode<TaskDispatcherNode, TaskDispatcherAppContext> {
	
	public TaskDispatcher() {
		// 监控中心
		appContext.setMStatReporter(new TaskDispatcherMStatReporter(appContext));
		// channel 管理者
		appContext.setChannelManager(new ChannelManager());
		appContext.setEventCenter(new InjvmEventCenter());
		appContext.setTaskPushMachine(new TaskPushMachine(appContext));
		appContext.setTaskReceiver(new TaskReceiver(appContext));
		// TaskExecuter 管理者
		appContext.setTaskExecuterManager(new TaskExecuterManager(appContext));
		// 添加节点变化监听器
		addNodeChangeListener(new TaskNodeChangeListener(appContext));
		// 添加master节点变化监听器
		addMasterChangeListener(new TaskDispatcherMasterChangeListener(appContext));
		
	}

	@Override
	protected void beforeStart() {
		// injectRemotingServer
		appContext.setRemotingServer(remotingClient);
		appContext.setJobLogger(new SmartJobLogger(appContext));

		TaskQueueFactory factory = ServiceLoader.load(TaskQueueFactory.class, config);
		appContext.setPoolQueue(factory.getPoolQueue(config));
		appContext.setExecutableJobQueue(factory.getExecutableJobQueue(config));
		appContext.setExecutingJobQueue(factory.getExecutingJobQueue(config));
		appContext.setTaskQueue(factory.getTaskQueue(config));
		appContext.setPreLoader(factory.getPreLoader(appContext));
		appContext.setTaskSender(new TaskSender(appContext));

		appContext.getHttpCmdServer().registerCommands(new LoadTaskHttpCmd(appContext), // 手动加载任务
				new AddTaskHttpCmd(appContext)); // 添加任务
	}

	@Override
	protected void afterStart() {
		appContext.getChannelManager().start();
		appContext.getMStatReporter().start();
	}


	@Override
	protected void afterStop() {
		appContext.getChannelManager().stop();
		appContext.getMStatReporter().stop();
		appContext.getHttpCmdServer().stop();
	}

	@Override
	protected void beforeStop() {
	}

	@Override
	protected RemotingProcessor getDefaultProcessor() {
		return new RemotingDispatcher(appContext);
	}

	/**
	 * 设置反馈数据给JobClient的负载均衡算法
	 */
	public void setLoadBalance(String loadBalance) {
		config.setParameter("loadbalance", loadBalance);
	}
}
