package com.tyyd.framework.dat.taskexecuter;

import com.tyyd.framework.dat.core.cluster.AbstractClientNode;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.ec.injvm.InjvmEventCenter;
import com.tyyd.framework.dat.remoting.RemotingProcessor;
import com.tyyd.framework.dat.taskdispatch.channel.ChannelManager;
import com.tyyd.framework.dat.taskexecuter.cmd.TaskTerminateCmd;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterAppContext;
import com.tyyd.framework.dat.taskexecuter.domain.TaskExecuterNode;
import com.tyyd.framework.dat.taskexecuter.monitor.TaskExecuterMStatReporter;
import com.tyyd.framework.dat.taskexecuter.processor.RemotingDispatcher;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerFactory;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerPool;

/**
 *         任务执行节点
 */
public class TaskExecuter extends AbstractClientNode<TaskExecuterNode, TaskExecuterAppContext> {
	
    public TaskExecuter() {
        appContext.setMStatReporter(new TaskExecuterMStatReporter(appContext));
    }

    @Override
    protected void beforeStart() {
        appContext.setRemotingServer(remotingServer);
        appContext.setRemotingClient(remotingClient);
        // channel 管理者
        appContext.setChannelManager(new ChannelManager());
        appContext.setEventCenter(new InjvmEventCenter());
        // 设置 线程池
        appContext.setRunnerPool(new RunnerPool(appContext));
        appContext.setMStatReporter(new TaskExecuterMStatReporter(appContext));
		

        appContext.getHttpCmdServer().registerCommands(
                new TaskTerminateCmd(appContext));     // 终止某个正在执行的任务
    }

    @Override
    protected void afterStart() {
    	appContext.getMStatReporter().start();
    }

    @Override
    protected void afterStop() {
        appContext.getMStatReporter().stop();
        appContext.getRunnerPool().shutDown();
    }

    @Override
    protected void beforeStop() {
    }

    @Override
    protected RemotingProcessor getDefaultProcessor() {
        return new RemotingDispatcher(appContext);
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
     * 设置taskRunner工场类，一般用户不用调用
     */
    public void setRunnerFactory(RunnerFactory factory) {
        appContext.setRunnerFactory(factory);
    }
}
