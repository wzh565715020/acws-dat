package com.tyyd.framework.dat.taskdispatch.support;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.utils.Holder;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.exception.RequestTimeoutException;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskPushRequest;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.ec.EventInfo;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.ResponseFuture;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;
import com.tyyd.framework.dat.taskdispatch.sender.TaskPushResult;
import com.tyyd.framework.dat.taskdispatch.sender.TaskSender;

/**
 * 任务分发管理
 */
public class TaskPusher {

	private final Logger LOGGER = LoggerFactory.getLogger(TaskPusher.class);
	private TaskDispatcherAppContext appContext;
	private TaskDispatcherMStatReporter stat;

	public TaskPusher(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
		this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();
	}

	public void push() {
		Node node = appContext.getRemotingClient().getTaskExecuterNode();
		if (node == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("taskExecuter didn't have node.");
			}
			EventInfo eventInfo = new EventInfo(EcTopic.NO_TASK_EXECUTER_AVAILABLE);
			appContext.getEventCenter().publishAsync(eventInfo);
			return;
		}
		String identity = node.getIdentity();
		int availableThreads = node.getAvailableThreads();
		if (availableThreads == 0) {
			return;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("taskTrackerIdentity:{} , availableThreads:{}", identity, availableThreads);
		}
		// 推送任务
		TaskPushResult result = send(appContext.getRemotingClient(), node);
		switch (result) {
		case SUCCESS:
			stat.incPushJobNum();
			break;
		case FAILED:
			// 还是要继续发送
			break;
		case NO_TASK:
			// 没有任务了
			return;
		case SENT_ERROR:
			// TaskTracker链接失败
			return;
		}
	}

	/**
	 * 是否推送成功
	 */
	private TaskPushResult send(final RemotingClientDelegate remotingClient, final Node taskTrackerNode) {

		final String identity = taskTrackerNode.getIdentity();

		TaskSender.SendResult sendResult = appContext.getTaskSender().send(identity, new TaskSender.SendInvoker() {
			@Override
			public TaskSender.SendResult invoke(final TaskPo taskPo) {
				// 发送给TaskTracker执行
				TaskPushRequest body = appContext.getCommandBodyWrapper().wrapper(new TaskPushRequest());
				body.setTaskMeta(TaskDomainConverter.convert(taskPo));
				RemotingCommand commandRequest = RemotingCommand
						.createRequestCommand(TaskProtos.RequestCode.PUSH_TASK.code(), body);
				// 是否分发推送任务成功
				final Holder<Boolean> pushSuccess = new Holder<Boolean>(false);

				final CountDownLatch latch = new CountDownLatch(1);
				try {
					remotingClient.invokeAsync(appContext,taskTrackerNode, commandRequest, new AsyncCallback() {
						@Override
						public void operationComplete(ResponseFuture responseFuture) {
							try {
								RemotingCommand responseCommand = responseFuture.getResponseCommand();
								if (responseCommand == null) {
									LOGGER.warn("task push failed! response command is null!");
									return;
								}
								if (responseCommand.getCode() == TaskProtos.ResponseCode.TASK_PUSH_SUCCESS.code()) {
									if (LOGGER.isDebugEnabled()) {
										LOGGER.debug("task push success! " + ", task=" + taskPo);
									}
									pushSuccess.set(true);
								}
							} finally {
								latch.countDown();
							}
						}
					});

				} catch (Exception e) {
					LOGGER.error("Remoting send error, taskPo={}", taskPo, e);
					rollBackData(taskPo);
					return new TaskSender.SendResult(false, TaskPushResult.SENT_ERROR);
				}

				try {
					latch.await(Constants.LATCH_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					throw new RequestTimeoutException(e);
				}

				if (!pushSuccess.get()) {
					rollBackData(taskPo);
					return new TaskSender.SendResult(false, TaskPushResult.SENT_ERROR);
				}

				return new TaskSender.SendResult(true, TaskPushResult.SUCCESS);
			}
		});

		return (TaskPushResult) sendResult.getReturnValue();
	}

	private void rollBackData(TaskPo taskPo) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("task push failed!" + ", identity=" + taskPo.getTaskExecuteNode() + ", task=" + taskPo);
		}
		// 队列切回来
		try {
			taskPo.setUpdateDate(SystemClock.now());
			appContext.getExecutableTaskQueue().add(taskPo);
		} catch (Exception e) {
			LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(taskPo));
		}
		appContext.getExecutingTaskQueue().remove(taskPo.getId());
	}

}
