package com.tyyd.framework.dat.taskdispatch;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.tyyd.framework.dat.biz.logger.SmartJobLogger;
import com.tyyd.framework.dat.core.cluster.AbstractClientNode;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.factory.NodeFactory;
import com.tyyd.framework.dat.core.listener.MasterChangeListener;
import com.tyyd.framework.dat.core.registry.NodeRegistryUtils;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.ec.EventInfo;
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
import com.tyyd.framework.dat.taskdispatch.support.cluster.TaskExecuterManager;
import com.tyyd.framework.dat.taskdispatch.support.listener.TaskNodeChangeListener;
import com.tyyd.framework.dat.zookeeper.DataListener;
import com.tyyd.framework.dat.taskdispatch.support.listener.TaskDispatcherMasterChangeListener;

public class TaskDispatcher extends AbstractClientNode<TaskDispatcherNode, TaskDispatcherAppContext> {

	private static String MASTER = "";
	// 调度器
	private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);

	public TaskDispatcher() {
		// 监控中心
		appContext.setMStatReporter(new TaskDispatcherMStatReporter(appContext));
		// channel 管理者
		appContext.setChannelManager(new ChannelManager());
		appContext.setTaskPushMachine(new TaskPushMachine(appContext));
		// TaskExecuter 管理者
		appContext.setTaskExecuterManager(new TaskExecuterManager(appContext));
		// 添加节点变化监听器
		addNodeChangeListener(new TaskNodeChangeListener(appContext));
		// 添加master节点变化监听器
		addMasterChangeListener(new TaskDispatcherMasterChangeListener(appContext));
		MASTER = NodeRegistryUtils.getNodeTypePath("MASTER", NodeType.TASK_DISPATCH) + "/MASTER";
	}

	@Override
	protected void beforeStart() {
		// injectRemotingServer
		appContext.setRemotingServer(remotingClient);
		appContext.setJobLogger(new SmartJobLogger(appContext));

		TaskQueueFactory factory = ServiceLoader.load(TaskQueueFactory.class, config);

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
		registry.addDataListener(MASTER, new DataListener() {

			@Override
			public void dataDeleted(String dataPath) throws Exception {
				Node node = appContext.getMasterNode();
				if (node != null && node.getIdentity().equals(appContext.getNode().getIdentity())) {// 若之前master为本机,则立即抢主,否则延迟5秒抢主(防止小故障引起的抢主可能导致的网络数据风暴)
					takeMaster();
				} else {
					delayExector.schedule(new Runnable() {
						@Override
						public void run() {
							takeMaster();
						}
					}, 10, TimeUnit.SECONDS);
				}
			}

			@Override
			public void dataChange(String dataPath, Object data) throws Exception {
				if (data instanceof Node) {
					appContext.setMasterNode((Node) data);
					notifyListener((Node) data);
				}
			}
		});
		Node newNode = NodeFactory.deepCopy(node);
		registry.updateRegister(MASTER, newNode);
	}
	private void notifyListener(Node master) {
        boolean isMaster = false;
        if (appContext.getConfig().getIdentity().equals(appContext.getMasterNode().getIdentity())) {
            LOGGER.info("Current node become the master node:{}", appContext.getMasterNode());
            isMaster = true;
        } else {
            LOGGER.info("Master node is :{}", appContext.getMasterNode());
            isMaster = false;
        }
        List<MasterChangeListener> listeners = getMasterChangeListener();
        if (listeners != null) {
            for (MasterChangeListener masterChangeListener : listeners) {
                try {
                    masterChangeListener.change(master, isMaster);
                } catch (Throwable t) {
                    LOGGER.error("MasterChangeListener notify error!", t);
                }
            }
        }
        EventInfo eventInfo = new EventInfo(EcTopic.MASTER_CHANGED);
        eventInfo.setParam("master", master);
        appContext.getEventCenter().publishSync(eventInfo);
    }
	private void takeMaster() {
		try {
			Node newNode = NodeFactory.deepCopy(node);
			registry.updateRegister(MASTER, newNode);
		} catch (Exception e) {
		}

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
