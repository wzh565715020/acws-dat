package com.tyyd.framework.dat.taskexecuter;

import com.tyyd.framework.dat.core.cluster.AbstractServerNode;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.registry.NodeRegistryUtils;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelManager;
import com.tyyd.framework.dat.taskexecuter.cmd.TaskTerminateCmd;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterNode;
import com.tyyd.framework.dat.taskexecuter.monitor.StopWorkingMonitor;
import com.tyyd.framework.dat.taskexecuter.monitor.TaskExecuterMStatReporter;
import com.tyyd.framework.dat.taskexecuter.processor.RemotingDispatcher;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;
import com.tyyd.framework.dat.zookeeper.DataListener;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerFactory;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerPool;

/**
 *         任务执行节点
 */
public class TaskExecuter extends AbstractServerNode<TaskExecuterNode, TaskExecuterAppContext> {
	private static String MASTER = "";
    public TaskExecuter() {
        appContext.setMStatReporter(new TaskExecuterMStatReporter(appContext));
        MASTER =  NodeRegistryUtils.getNodeTypePath("MASTER", NodeType.TASK_DISPATCH) + "/MASTER";
    }

    @Override
    protected void beforeStart() {
        appContext.setRemotingServer(remotingServer);
        // channel 管理者
        appContext.setChannelManager(new ChannelManager());
        // 设置 线程池
        appContext.setRunnerPool(new RunnerPool(appContext));
        appContext.getMStatReporter().start();
        appContext.setStopWorkingMonitor(new StopWorkingMonitor(appContext));

        appContext.getHttpCmdServer().registerCommands(
                new TaskTerminateCmd(appContext));     // 终止某个正在执行的任务
    }

    @Override
    protected void afterStart() {
        if (config.getParameter(Constants.TASK_EXECUTER_STOP_WORKING_ENABLE, false)) {
            appContext.getStopWorkingMonitor().start();
        }
    }

    @Override
    protected void afterStop() {
        appContext.getMStatReporter().stop();
        appContext.getStopWorkingMonitor().stop();
        appContext.getRunnerPool().shutDown();
        registry.addDataListener(MASTER, new DataListener() {
			
			@Override
			public void dataDeleted(String dataPath) throws Exception {
				
			}
			
			@Override
			public void dataChange(String dataPath, Object data) throws Exception {
				if (data instanceof Node) {
					appContext.setMasterNode((Node)data);
				} 
			}
		});
    }

    @Override
    protected void beforeStop() {
    }

    @Override
    protected RemotingProcessor getDefaultProcessor() {
        return new RemotingDispatcher(appContext);
    }

    public <JRC extends TaskRunner> void setJobRunnerClass(Class<JRC> clazz) {
        appContext.setJobRunnerClass(clazz);
    }

    public void setWorkThreads(int workThreads) {
        config.setWorkThreads(workThreads);
    }

    /**
     * 设置业务日志记录级别
     */
    public void setBizLoggerLevel(Level level) {
        if (level != null) {
            appContext.setBizLogLevel(level);
        }
    }

    /**
     * 设置JobRunner工场类，一般用户不用调用
     */
    public void setRunnerFactory(RunnerFactory factory) {
        appContext.setRunnerFactory(factory);
    }
}
