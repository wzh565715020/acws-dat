package com.tyyd.framework.dat.taskdispatch.support.listener;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.listener.MasterChangeListener;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.support.checker.ExecutingDeadTaskChecker;

/**
 *         TaskDispatcher master 节点变化之后
 */
public class TaskDispatcherMasterChangeListener implements MasterChangeListener {

    private TaskDispatcherAppContext appContext;
    private ExecutingDeadTaskChecker executingDeadTaskChecker;

    public TaskDispatcherMasterChangeListener(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.executingDeadTaskChecker = new ExecutingDeadTaskChecker(appContext);
        this.appContext.setExecutingDeadJobChecker(executingDeadTaskChecker);
    }

    @Override
    public void change(Node master, boolean isMaster) {

        if (appContext.getConfig().getIdentity().equals(master.getIdentity())) {
            // 如果 master 节点是自己, 启动通知客户端失败检查重发的定时器
            executingDeadTaskChecker.start();
            appContext.getTaskPushMachine().start();
            appContext.getTaskReceiver().start();
        } else {
            // 如果 master 节点不是自己,关闭通知客户端失败检查重发的定时器
            executingDeadTaskChecker.stop();
            appContext.getTaskPushMachine().stop();
        }
    }
}
