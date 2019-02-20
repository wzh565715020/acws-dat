package com.tyyd.framework.dat.taskexecuter.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.remoting.ChannelWrapper;

/**
 * 管理channel
 */
public class TaskExecuterChannelManager {

	private final Logger LOGGER = LoggerFactory.getLogger(TaskExecuterChannelManager.class);

	// 客户端列表 (要保证同一个group的node要是无状态的)
	private List<ChannelWrapper> taskDispatcherChannelList = new CopyOnWriteArrayList<>();
	// 任务节点列表
	// 用来定时检查已经关闭的channel
	private final ScheduledExecutorService channelCheckExecutorService = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("DAT-Channel-Checker", true));

	private ScheduledFuture<?> scheduledFuture;

	// 存储离线一定时间内的节点信息
	private final ConcurrentHashMap<String, Long> offlineTaskExecuterMap = new ConcurrentHashMap<String, Long>();
	// 用来清理离线时间很长的信息
	private final ScheduledExecutorService offlineTaskExecuterCheckExecutorService = Executors.newScheduledThreadPool(1,
			new NamedThreadFactory("DAT-offline-TaskExecuter-Checker", true));

	private ScheduledFuture<?> offlineTaskTrackerScheduledFuture;

	private AtomicBoolean start = new AtomicBoolean(false);

	public void start() {
		try {
			if (start.compareAndSet(false, true)) {
				scheduledFuture = channelCheckExecutorService.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						try {
							checkCloseChannel(taskDispatcherChannelList);
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("TASK_EXECUTER Channel Pool " + taskDispatcherChannelList);
							}
						} catch (Throwable t) {
							LOGGER.error("Check channel error!", t);
						}
					}
				}, 10, 10, TimeUnit.SECONDS);

				offlineTaskTrackerScheduledFuture = offlineTaskExecuterCheckExecutorService
						.scheduleWithFixedDelay(new Runnable() {
							@Override
							public void run() {
								try {
									if (offlineTaskExecuterMap.size() > 0) {
										for (Map.Entry<String, Long> entry : offlineTaskExecuterMap.entrySet()) {
											// 清除离线超过一定时间的信息
											if (SystemClock.now() - entry.getValue() > 2
													* Constants.DEFAULT_TASK_EXECUTER_OFFLINE_LIMIT_MILLIS) {
												offlineTaskExecuterMap.remove(entry.getKey());
											}
										}
									}
								} catch (Throwable t) {
									LOGGER.error("Check offline channel error!", t);
								}
							}
						}, 1, 1, TimeUnit.MINUTES); // 1分钟检查一次

			}
			LOGGER.info("Start channel manager success!");
		} catch (Throwable t) {
			LOGGER.error("Start channel manager failed!", t);
		}
	}

	public void stop() {
		try {
			if (start.compareAndSet(true, false)) {
				scheduledFuture.cancel(true);
				channelCheckExecutorService.shutdown();
				offlineTaskTrackerScheduledFuture.cancel(true);
				offlineTaskExecuterCheckExecutorService.shutdown();
			}
			LOGGER.info("Stop channel manager success!");
		} catch (Throwable t) {
			LOGGER.error("Stop channel manager failed!", t);
		}
	}

	/**
	 * 检查 关闭的channel
	 */
	private void checkCloseChannel(List<ChannelWrapper> channels) {
		List<ChannelWrapper> removeList = new ArrayList<ChannelWrapper>();
		for (ChannelWrapper channel : channels) {
			if (channel.isClosed()) {
				removeList.add(channel);
				LOGGER.info("close channel={}", channel);
			}
		}
		channels.removeAll(removeList);
		// 加入到离线列表中
		for (ChannelWrapper channelWrapper : removeList) {
			offlineTaskExecuterMap.put(channelWrapper.getIdentity(), SystemClock.now());
		}
	}

	public List<ChannelWrapper> getChannels() {
		return taskDispatcherChannelList;
	}

	/**
	 * 根据 节点唯一编号得到 channel
	 */
	public ChannelWrapper getChannel(NodeType nodeType, String identity) {
		List<ChannelWrapper> channelWrappers = getChannels();
		if (channelWrappers != null && channelWrappers.size() != 0) {
			for (ChannelWrapper channelWrapper : channelWrappers) {
				if (channelWrapper.getIdentity().equals(identity)) {
					return channelWrapper;
				}
			}
		}
		return null;
	}

	/**
	 * 添加channel
	 */
	public void offerChannel(ChannelWrapper channel) {
		List<ChannelWrapper> channels = getChannels();
		if (!channels.contains(channel)) {
			channels.add(channel);
			LOGGER.info("new connected channel={}", channel);
		}
	}

	public Long getOfflineTimestamp(String identity) {
		return offlineTaskExecuterMap.get(identity);
	}

	public void removeChannel(ChannelWrapper channel) {
		List<ChannelWrapper> channels = getChannels();
		if (channels != null) {
			channels.remove(channel);
			LOGGER.info("remove channel={}", channel);
		}
	}
}
