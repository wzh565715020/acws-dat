package com.tyyd.framework.dat.taskdispatch.support;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.utils.Holder;
import com.tyyd.framework.dat.core.commons.utils.ResourceUtils;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.exception.RequestTimeoutException;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskPushRequest;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.PreLoader;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.MysqlPreLoader;
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

	private AtomicBoolean start = new AtomicBoolean(false);

	private Runnable worker;

	private String poolId;

	private PreLoader preLoader;

	private int taskPushFrequency;

	private ScheduledFuture<?> scheduledFuture;

	private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("dat-TaskPushMachine-Executor", true));

	public TaskPusher(TaskDispatcherAppContext appContext, String poolId) {
		this.appContext = appContext;
		this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();
		this.preLoader = new MysqlPreLoader(appContext, poolId);
		this.poolId = poolId;
		this.taskPushFrequency = appContext.getConfig().getParameter(Constants.TASK_PUSH_FREQUENCY,
				Constants.DEFAULT_TASK_PUSH_FREQUENCY);
		this.worker = new Runnable() {
			@Override
			public void run() {
				try {
					if (!start.get()) {
						return;
					}
					if (!ResourceUtils.isMachineResEnough(appContext)) {
						// 如果机器资源不足,那么不分配任务
						return;
					}
					push();
				} catch (Exception e) {
					LOGGER.error("task pull machine run error!", e);
				}
			}
		};
	}

	public void start() {
		try {
			if (start.compareAndSet(false, true)) {
				if (scheduledFuture == null) {
					scheduledFuture = SCHEDULED_CHECKER.scheduleWithFixedDelay(worker, 1, taskPushFrequency,
							TimeUnit.SECONDS);
				}
				LOGGER.info("Start task push machine success!");
			}
		} catch (Throwable t) {
			LOGGER.error("Start stask push machine failed!", t);
		}
	}

	public void stop() {
		try {
			if (start.compareAndSet(true, false)) {
				preLoader.stop();
				scheduledFuture.cancel(false);
				SCHEDULED_CHECKER.shutdown();
				scheduledFuture = null;
				LOGGER.info("Start task push machine success!");
			}
		} catch (Throwable t) {
			LOGGER.error("Start stask push machine failed!", t);
		}
	}

	public void push() {
		LOGGER.info(appContext.getConfig().getIdentity() + "分发任务开始");
		Node node = appContext.getTaskExecuterManager().getTaskExecuterNode();
		if (node == null) {
			LOGGER.info("taskExecuter didn't have node.");
			return;
		}
		String identity = node.getIdentity();
		int availableThreads = node.getAvailableThreads();
		if (availableThreads == 0) {
			return;
		}
		LOGGER.info("taskTrackerIdentity:{} , availableThreads:{}", identity, availableThreads);
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
		default:
			break;
		}
		LOGGER.info(appContext.getConfig().getIdentity() + "分发任务结束");
	}

	/**
	 * 是否推送成功
	 */
	private TaskPushResult send(final RemotingClientDelegate remotingClient, final Node taskTrackerNode) {

		final String identity = taskTrackerNode.getIdentity();

		TaskSender.SendResult sendResult = appContext.getTaskSender().send(preLoader, identity,
				new TaskSender.SendInvoker() {
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
							remotingClient.invokeAsync(appContext, taskTrackerNode, commandRequest,
									new AsyncCallback() {
										@Override
										public void operationComplete(ResponseFuture responseFuture) {
											try {
												RemotingCommand responseCommand = responseFuture.getResponseCommand();
												if (responseCommand == null) {
													LOGGER.warn("task push failed! response command is null!");
													return;
												}
												if (responseCommand
														.getCode() == TaskProtos.ResponseCode.TASK_PUSH_SUCCESS
																.code()) {
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
		LOGGER.info("task push failed!" + ", identity=" + taskPo.getTaskExecuteNode() + ", task=" + taskPo);
		// 队列切回来
		try {
			taskPo.setUpdateDate(SystemClock.now());
			appContext.getExecutableTaskQueue().add(taskPo);
		} catch (Exception e) {
			LOGGER.warn("ExecutableJobQueue already exist:" + JSON.toJSONString(taskPo));
		}
		appContext.getExecutingTaskQueue().remove(taskPo.getId());
	}

	public String getPoolId() {
		return poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}

	public AtomicBoolean getStart() {
		return start;
	}

}
