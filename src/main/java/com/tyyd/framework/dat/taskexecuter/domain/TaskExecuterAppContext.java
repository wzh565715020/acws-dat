package com.tyyd.framework.dat.taskexecuter.domain;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.taskexecuter.monitor.StopWorkingMonitor;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerFactory;
import com.tyyd.framework.dat.taskexecuter.runner.RunnerPool;
import com.tyyd.framework.dat.taskexecuter.support.TaskPullMachine;

public class TaskExecuterAppContext extends AppContext {

    private RemotingClientDelegate remotingClient;
    // runner 线程池
    private RunnerPool runnerPool;
    //
    private RunnerFactory runnerFactory;
    // Pull Job Machine
    private TaskPullMachine jobPullMachine;

    private StopWorkingMonitor stopWorkingMonitor;
    /**
     * 业务日志记录级别
     */
    private Level bizLogLevel;
    /**
     * 执行任务的class
     */
    private Class<?> jobRunnerClass;

    public StopWorkingMonitor getStopWorkingMonitor() {
        return stopWorkingMonitor;
    }

    public void setStopWorkingMonitor(StopWorkingMonitor stopWorkingMonitor) {
        this.stopWorkingMonitor = stopWorkingMonitor;
    }

    public RunnerPool getRunnerPool() {
        return runnerPool;
    }

    public void setRunnerPool(RunnerPool runnerPool) {
        this.runnerPool = runnerPool;
    }

    public Level getBizLogLevel() {
        return bizLogLevel;
    }

    public void setBizLogLevel(Level bizLogLevel) {
        this.bizLogLevel = bizLogLevel;
    }

    public Class<?> getJobRunnerClass() {
        return jobRunnerClass;
    }

    public void setJobRunnerClass(Class<?> jobRunnerClass) {
        this.jobRunnerClass = jobRunnerClass;
    }

    public RunnerFactory getRunnerFactory() {
        return runnerFactory;
    }

    public void setRunnerFactory(RunnerFactory runnerFactory) {
        this.runnerFactory = runnerFactory;
    }

    public RemotingClientDelegate getRemotingClient() {
        return remotingClient;
    }

    public void setRemotingClient(RemotingClientDelegate remotingClient) {
        this.remotingClient = remotingClient;
    }

    public TaskPullMachine getJobPullMachine() {
        return jobPullMachine;
    }

    public void setJobPullMachine(TaskPullMachine jobPullMachine) {
        this.jobPullMachine = jobPullMachine;
    }
}
