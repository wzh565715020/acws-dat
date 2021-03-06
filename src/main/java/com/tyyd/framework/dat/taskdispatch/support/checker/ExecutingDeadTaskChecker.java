package com.tyyd.framework.dat.taskdispatch.support.checker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.alibaba.fastjson.JSON;
import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.exception.RemotingSendException;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.protocol.TaskProtos;
import com.tyyd.framework.dat.core.protocol.command.TaskAskRequest;
import com.tyyd.framework.dat.core.protocol.command.TaskAskResponse;
import com.tyyd.framework.dat.core.remoting.RemotingClientDelegate;
import com.tyyd.framework.dat.core.support.TaskDomainConverter;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.remoting.AsyncCallback;
import com.tyyd.framework.dat.remoting.Channel;
import com.tyyd.framework.dat.remoting.ChannelWrapper;
import com.tyyd.framework.dat.remoting.ResponseFuture;
import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;
import com.tyyd.framework.dat.remoting.protocol.RemotingProtos;
import com.tyyd.framework.dat.store.transaction.SpringContextHolder;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;
import com.tyyd.framework.dat.taskdispatch.monitor.TaskDispatcherMStatReporter;

/**
 * 死掉的任务 1. 分发出去的，并且执行节点不存在的任务 2. 分发出去，执行节点还在, 但是没有在执行的任务
 */
public class ExecutingDeadTaskChecker {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutingDeadTaskChecker.class);

	private final ScheduledExecutorService FIXED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("DAT-ExecutingTaskQueue-Fix-Executor", true));

	private TaskDispatcherAppContext appContext;

	private TaskDispatcherMStatReporter stat;

	private AtomicBoolean start = new AtomicBoolean(false);

	private ScheduledFuture<?> scheduledFuture;

	public ExecutingDeadTaskChecker(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
		this.stat = (TaskDispatcherMStatReporter) appContext.getMStatReporter();
	}

	public void start() {
		try {
			if (start.compareAndSet(false, true)) {
				int fixCheckPeriodSeconds = appContext.getConfig()
						.getParameter("TaskDispatcher.executing.task.fix.check.interval.seconds", 30);
				if (fixCheckPeriodSeconds < 5) {
					fixCheckPeriodSeconds = 5;
				} else if (fixCheckPeriodSeconds > 5 * 60) {
					fixCheckPeriodSeconds = 5 * 60;
				}
				scheduledFuture = FIXED_EXECUTOR_SERVICE.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						try {
							// 判断注册中心是否可用，如果不可用，那么直接返回，不进行处理
							if (!appContext.getRegistryStatMonitor().isAvailable()) {
								return;
							}
							checkAndFix();
						} catch (Throwable t) {
							LOGGER.error("Check executing dead task error ", t);
						}
					}
				}, fixCheckPeriodSeconds, fixCheckPeriodSeconds, TimeUnit.SECONDS);
			}
			LOGGER.info("Executing dead task checker started!");
		} catch (Throwable e) {
			LOGGER.error("Executing dead task checker start failed!", e);
		}
	}

	private void checkAndFix() throws RemotingSendException {

		// 30s没有收到反馈信息，需要去检查这个任务是否还在执行
		int maxDeadCheckTime = appContext.getConfig().getParameter("taskdispatcher.executing.task.fix.deadline.seconds",
				20);
		if (maxDeadCheckTime < 10) {
			maxDeadCheckTime = 10;
		} else if (maxDeadCheckTime > 5 * 60) {
			maxDeadCheckTime = 5 * 60;
		}

		// 查询出所有死掉的任务 (其实可以直接在数据库中fix的, 查询出来主要是为了日志打印)
		// 一般来说这个是没有多大的，我就不分页去查询了
		List<TaskPo> maybeDeadJobPos = appContext.getExecutingTaskQueue()
				.getDeadTasks(SystemClock.now() - maxDeadCheckTime * 1000);
		if (CollectionUtils.isEmpty(maybeDeadJobPos)) {
			return;
		}
		Map<String, List<TaskPo>> taskMap = new HashMap<String, List<TaskPo>>();
		for (TaskPo taskPo : maybeDeadJobPos) {
			List<TaskPo> taskPos = taskMap.get(taskPo.getTaskExecuteNode());
			if (taskPos == null) {
				taskPos = new ArrayList<TaskPo>();
				taskMap.put(taskPo.getTaskExecuteNode(), taskPos);
			}
			taskPos.add(taskPo);
		}

		for (Map.Entry<String, List<TaskPo>> entry : taskMap.entrySet()) {
			String taskExecuterIdentity = entry.getKey();
			// 去询问是否在执行该任务
			askTimeoutTask(taskExecuterIdentity, entry.getValue());
		}
	}

	/**
	 * 向taskExecuter询问执行中的任务
	 */
	private void askTimeoutTask(String taskExecuterIdentity, final List<TaskPo> taskPos) {
		try {

			ChannelWrapper channelWrapper = appContext.getChannelManager().getChannel(NodeType.TASK_EXECUTER,
					taskExecuterIdentity);
			Node node = appContext.getSubscribedNodeManager().getNode(NodeType.TASK_EXECUTER, taskExecuterIdentity);
			Channel channel = null;
			String address = null;
			if (channelWrapper != null) {
				channel = channelWrapper.getChannel();
			}
			if (channel == null && address == null) {
				Long maxUpdateDate = null;
				for (TaskPo taskPo : taskPos) {
					if (maxUpdateDate == null) {
						maxUpdateDate = taskPo.getUpdateDate();
						continue;
					}
					if (taskPo.getUpdateDate() > maxUpdateDate) {
						maxUpdateDate = taskPo.getUpdateDate();
					}
				}
				if (SystemClock.now() - maxUpdateDate > 3 * 60 * 60 * 1000) {
					fixDeadTask(taskPos);
				}
				return;
			}
			List<String> ids = new ArrayList<String>(taskPos.size());
			for (TaskPo taskPo : taskPos) {
				ids.add(taskPo.getId());
			}
			RemotingClientDelegate remotingclient = appContext.getRemotingClient();

			TaskAskRequest requestBody = appContext.getCommandBodyWrapper().wrapper(new TaskAskRequest());

			requestBody.setIds(ids);

			RemotingCommand request = RemotingCommand.createRequestCommand(TaskProtos.RequestCode.TASK_ASK.code(),
					requestBody);

			remotingclient.invokeAsync(appContext, node, channel, request, new AsyncCallback() {
				@Override
				public void operationComplete(ResponseFuture responseFuture) {
					RemotingCommand response = responseFuture.getResponseCommand();
					if (response != null && RemotingProtos.ResponseCode.SUCCESS.code() == response.getCode()) {
						TaskAskResponse responseBody = response.getBody();
						List<String> deadIds = responseBody.getIds();
						if (CollectionUtils.isNotEmpty(deadIds)) {
							try {
								// 睡了1秒再修复, 防止任务刚好执行完正在传输中. 1s可以让完成的正常完成
								Thread.sleep(1000L);
							} catch (InterruptedException ignored) {
							}
							for (TaskPo taskPo : taskPos) {
								if (deadIds.contains(taskPo.getId())) {
									fixDeadTask(taskPo);
								}
							}
						}
					}
				}
			});
		} catch (Exception e) {
			LOGGER.error("Ask timeout task error, ", e);
		}

	}

	private void fixDeadTask(List<TaskPo> taskPos) {
		for (TaskPo taskPo : taskPos) {
			fixDeadTask(taskPo);
		}
	}

	private void fixDeadTask(TaskPo taskPo) {
		ApplicationContext applicationContext = SpringContextHolder.getApplicationContext();
		DataSourceTransactionManager dataSourceTransactionManager = applicationContext
				.getBean(DataSourceTransactionManager.class);
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); // 事物隔离级别，开启新事务，这样会比较安全些。
		TransactionStatus status = dataSourceTransactionManager.getTransaction(def); // 获得事务状态
		try {
			taskPo.setIsRunning(0);
			taskPo.setUpdateDate(SystemClock.now());
			taskPo.setTaskExecuteNode(null);
			// 1. add to executable queue
			if (!taskPo.isCronExpression() && !taskPo.isRepeatableExpression()) {
				appContext.getExecutableTaskQueue().add(taskPo);
			} else {
				appContext.getExecutableTaskQueue().resume(taskPo);
			}

			// 2. remove from executing queue
			appContext.getExecutingTaskQueue().remove(taskPo.getId());

			TaskLogPo jobLogPo = TaskDomainConverter.convertTaskLog(taskPo);
			jobLogPo.setSuccess(true);
			jobLogPo.setLevel(Level.WARN);
			jobLogPo.setLogType(LogType.FIXED_DEAD);
			appContext.getTaskLogger().log(jobLogPo);

			stat.incFixExecutingJobNum();
			dataSourceTransactionManager.commit(status);
		} catch (Throwable t) {
			dataSourceTransactionManager.rollback(status);
			LOGGER.error(t.getMessage(), t);
		}
		LOGGER.info("checkAndFix dead task ! {}", JSON.toJSONString(taskPo));
	}

	public void stop() {
		try {
			if (start.compareAndSet(true, false)) {
				scheduledFuture.cancel(true);
				FIXED_EXECUTOR_SERVICE.shutdown();
			}
			LOGGER.info("Executing dead task checker stopped!");
		} catch (Throwable t) {
			LOGGER.error("Executing dead task checker stop failed!", t);
		}
	}

}
