package com.tyyd.framework.dat.taskdispatch.support.listener;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.listener.MasterChangeListener;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.support.checker.ExecutableDeadTaskChecker;
import com.tyyd.framework.dat.taskdispatch.support.checker.ExecutingDeadTaskChecker;
import com.tyyd.framework.dat.taskdispatch.support.checker.FeedbackTaskSendChecker;

/**
 *         JobTracker master 节点变化之后
 */
public class TaskDispatcherMasterChangeListener implements MasterChangeListener {

    private TaskDispatcherAppContext appContext;
    private ExecutingDeadTaskChecker executingDeadJobChecker;
    private FeedbackTaskSendChecker feedbackJobSendChecker;
    private ExecutableDeadTaskChecker executableDeadJobChecker;

    public TaskDispatcherMasterChangeListener(TaskDispatcherAppContext appContext) {
        this.appContext = appContext;
        this.executingDeadJobChecker = new ExecutingDeadTaskChecker(appContext);
        this.appContext.setExecutingDeadJobChecker(executingDeadJobChecker);
        this.feedbackJobSendChecker = new FeedbackTaskSendChecker(appContext);
        this.executableDeadJobChecker = new ExecutableDeadTaskChecker(appContext);
    }

    @Override
    public void change(Node master, boolean isMaster) {

        if (appContext.getConfig().getIdentity().equals(master.getIdentity())) {
            // 如果 master 节点是自己
            // 2. 启动通知客户端失败检查重发的定时器
            feedbackJobSendChecker.start();
            executingDeadJobChecker.start();
            executableDeadJobChecker.start();
        } else {
            // 如果 master 节点不是自己

            // 2. 关闭通知客户端失败检查重发的定时器
            feedbackJobSendChecker.stop();
            executingDeadJobChecker.stop();
            executableDeadJobChecker.stop();
        }
    }
}
