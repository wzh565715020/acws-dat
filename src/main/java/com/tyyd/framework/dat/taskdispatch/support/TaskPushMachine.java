package com.tyyd.framework.dat.taskdispatch.support;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.constant.EcTopic;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.ec.EventInfo;
import com.tyyd.framework.dat.ec.EventSubscriber;
import com.tyyd.framework.dat.ec.Observer;
import com.tyyd.framework.dat.jvmmonitor.JVMConstants;
import com.tyyd.framework.dat.jvmmonitor.JVMMonitor;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandFieldCheckException;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskPushMachine {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskPushMachine.class.getSimpleName());

	// 定时检查TaskExecuter是否有空闲的线程，如果有，那么发出任务请求
	private final ScheduledExecutorService SCHEDULED_CHECKER = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("dat-TaskPushMachine-Executor", true));
	private ScheduledFuture<?> scheduledFuture;
	private AtomicBoolean start = new AtomicBoolean(false);
	private TaskDispatcherAppContext appContext;
	private Runnable worker;
	private int taskPushFrequency;
	// 是否启用机器资源检查
	private boolean machineResCheckEnable = true;
    
	private TaskPusher taskPusher;

	public TaskPushMachine(final TaskDispatcherAppContext appContext) {

		this.appContext = appContext;
		taskPusher = new TaskPusher(appContext);
		this.taskPushFrequency = appContext.getConfig().getParameter(Constants.TASK_PUSH_FREQUENCY,
				Constants.DEFAULT_TASK_PUSH_FREQUENCY);

		this.machineResCheckEnable = appContext.getConfig().getParameter(Constants.LB_MACHINE_RES_CHECK_ENABLE, true);

		appContext.getEventCenter().subscribe(new EventSubscriber(
				TaskPushMachine.class.getSimpleName().concat(appContext.getConfig().getIdentity()), new Observer() {
					@Override
					public void onObserved(EventInfo eventInfo) {
						if (EcTopic.TASK_EXECUTER_AVAILABLE.equals(eventInfo.getTopic())) {
							// TASK_EXECUTER 可用了
							start();
						} else if (EcTopic.NO_TASK_EXECUTER_AVAILABLE.equals(eventInfo.getTopic())) {
							stop();
						}
					}
				}), EcTopic.TASK_EXECUTER_AVAILABLE, EcTopic.NO_TASK_EXECUTER_AVAILABLE);
		this.worker = new Runnable() {
			@Override
			public void run() {
				try {
					if (!start.get()) {
						return;
					}
					if (!isMachineResEnough()) {
						// 如果机器资源不足,那么不去取任务
						return;
					}
					sendRequest();
				} catch (Exception e) {
					LOGGER.error("task pull machine run error!", e);
				}
			}
		};
	}

	public void start() {
		try {
			if (appContext.getConfig().getIdentity().equals(appContext.getMasterNode().getIdentity()) && start.compareAndSet(false, true)) {
				PoolQueueReq request = new PoolQueueReq();
				request.setLimit(Integer.MAX_VALUE);
				List<PoolPo> list = appContext.getPoolQueue().pageSelect(request).getRows();
				appContext.setPoolPoList(new CopyOnWriteArrayList<PoolPo>());
				if (list!=null && list.size()>0) {
					appContext.getPoolPoList().addAll(list);
				}
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

	private void stop() {
		try {
			if (start.compareAndSet(true, false)) {
				scheduledFuture.cancel(true);
				// SCHEDULED_CHECKER.shutdown();
				LOGGER.info("Stop task push  machine success!");
			}
		} catch (Throwable t) {
			LOGGER.error("Stop task push machine failed!", t);
		}
	}

	/**
	 * 发送task push 请求
	 */
	private void sendRequest() throws RemotingCommandFieldCheckException {
		taskPusher.push();
	}

	/**
	 * 查看当前机器资源是否足够
	 */
	private boolean isMachineResEnough() {

		if (!machineResCheckEnable) {
			// 如果没有启用,直接返回
			return true;
		}

		boolean enough = true;

		try {
			// 1. Cpu usage
			Double maxCpuTimeRate = appContext.getConfig().getParameter(Constants.LB_CPU_USED_RATE_MAX, 90d);
			Object processCpuTimeRate = JVMMonitor.getAttribute(JVMConstants.JMX_JVM_THREAD_NAME, "ProcessCpuTimeRate");
			if (processCpuTimeRate != null) {
				Double cpuRate = Double.valueOf(processCpuTimeRate.toString()) / (Constants.AVAILABLE_PROCESSOR * 1.0);
				if (cpuRate >= maxCpuTimeRate) {
					LOGGER.info("Pause Pull, CPU USAGE is " + String.format("%.2f", cpuRate) + "% >= "
							+ String.format("%.2f", maxCpuTimeRate) + "%");
					enough = false;
					return false;
				}
			}

			// 2. Memory usage
			Double maxMemoryUsedRate = appContext.getConfig().getParameter(Constants.LB_MEMORY_USED_RATE_MAX, 90d);
			Runtime runtime = Runtime.getRuntime();
			long maxMemory = runtime.maxMemory();
			long usedMemory = runtime.totalMemory() - runtime.freeMemory();

			Double memoryUsedRate = new BigDecimal(usedMemory / maxMemory, new MathContext(4)).doubleValue();

			if (memoryUsedRate >= maxMemoryUsedRate) {
				LOGGER.info("Pause Pull, MEMORY USAGE is " + memoryUsedRate + " >= " + maxMemoryUsedRate);
				enough = false;
				return false;
			}
			enough = true;
			return true;
		} catch (Exception e) {
			LOGGER.warn("Check Machine Resource error", e);
			return true;
		} finally {
			Boolean machineResEnough = appContext.getConfig().getInternalData(Constants.MACHINE_RES_ENOUGH, true);
			if (machineResEnough != enough) {
				appContext.getConfig().setInternalData(Constants.MACHINE_RES_ENOUGH, enough);
			}
		}
	}

}
