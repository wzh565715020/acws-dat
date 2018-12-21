package com.tyyd.framework.dat.core.support;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.GenericsUtils;
import com.tyyd.framework.dat.core.domain.Pair;
import com.tyyd.framework.dat.core.factory.NamedThreadFactory;
import com.tyyd.framework.dat.core.failstore.FailStore;
import com.tyyd.framework.dat.core.failstore.FailStoreException;
import com.tyyd.framework.dat.core.failstore.FailStoreFactory;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.spi.ServiceLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 重试定时器 (用来发送 给 客户端的反馈信息等)
 */
public abstract class RetryScheduler<T> {

	public static final Logger LOGGER = LoggerFactory.getLogger(RetryScheduler.class);

	private Class<?> type = GenericsUtils.getSuperClassGenericType(this.getClass());

	// 定时检查是否有 师表的反馈任务信息(给客户端的)
	private ScheduledExecutorService RETRY_EXECUTOR_SERVICE = Executors
			.newSingleThreadScheduledExecutor(new NamedThreadFactory("DAT-RetryScheduler-retry", true));
	
	private ScheduledFuture<?> scheduledFuture;
	private AtomicBoolean selfCheckStart = new AtomicBoolean(false);
	private FailStore failStore;
	// 名称主要是用来记录日志
	private String name;

	// 批量发送的消息数
	private int batchSize = 5;

	private ReentrantLock lock = new ReentrantLock();
	private AppContext appContext;

	public RetryScheduler(AppContext appContext) {
		this(appContext, appContext.getConfig().getFailStorePath());
	}

	public RetryScheduler(final AppContext appContext, String storePath) {
		this.appContext = appContext;
		FailStoreFactory failStoreFactory = ServiceLoader.load(FailStoreFactory.class, appContext.getConfig());
		failStore = failStoreFactory.getFailStore(appContext.getConfig(), storePath);
		try {
			failStore.open();
		} catch (FailStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public RetryScheduler(AppContext appContext, String storePath, int batchSize) {
		this(appContext, storePath);
		this.batchSize = batchSize;
	}

	protected RetryScheduler(AppContext appContext, int batchSize) {
		this(appContext);
		this.batchSize = batchSize;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void start() {
		try {
			if (selfCheckStart.compareAndSet(false, true)) {
				// 这个时间后面再去优化
				scheduledFuture = RETRY_EXECUTOR_SERVICE.scheduleWithFixedDelay(new CheckSelfRunner(), 10, 30,
						TimeUnit.SECONDS);
				LOGGER.info("Start {} RetryScheduler success, identity=[{}]", name,
						appContext.getConfig().getIdentity());
			}
		} catch (Throwable t) {
			LOGGER.error("Start {} RetryScheduler failed, identity=[{}]", name, appContext.getConfig().getIdentity(),
					t);
		}
	}

	public void stop() {
		try {
			if (selfCheckStart.compareAndSet(true, false)) {
				if (scheduledFuture != null) {
					scheduledFuture.cancel(true);
					failStore.close();
					RETRY_EXECUTOR_SERVICE.shutdown();
				}
				LOGGER.info("Stop {} RetryScheduler success, identity=[{}]", name,
						appContext.getConfig().getIdentity());
			}
		} catch (Throwable t) {
			LOGGER.error("Stop {} RetryScheduler failed, identity=[{}]", name, appContext.getConfig().getIdentity(), t);
		}
	}

	public void destroy() {
		try {
			stop();
			failStore.destroy();
		} catch (FailStoreException e) {
			LOGGER.error("destroy {} RetryScheduler failed, identity=[{}]", name, appContext.getConfig().getIdentity(),
					e);
		}
	}

	/**
	 * 定时检查 提交失败任务的Runnable
	 */
	private class CheckSelfRunner implements Runnable {

		@Override
		public void run() {
			try {
				List<Pair<String, T>> pairs = null;
				do {
					try {
						lock.tryLock(1000, TimeUnit.MILLISECONDS);
						pairs = failStore.fetchTop(batchSize, type);

						if (CollectionUtils.isEmpty(pairs)) {
							break;
						}

						List<T> values = new ArrayList<T>(pairs.size());
						List<String> keys = new ArrayList<String>(pairs.size());
						for (Pair<String, T> pair : pairs) {
							keys.add(pair.getKey());
							values.add(pair.getValue());
						}
						if (retry(values)) {
							LOGGER.info("{} RetryScheduler, local files send success, identity=[{}], size: {}, {}",
									name, appContext.getConfig().getIdentity(), values.size(),
									JSON.toJSONString(values));
							failStore.delete(keys);
						} else {
							break;
						}
					} finally {
						if (lock.isHeldByCurrentThread()) {
							lock.unlock();
						}
					}
				} while (CollectionUtils.isNotEmpty(pairs));

			} catch (Throwable e) {
				LOGGER.error("Run {} RetryScheduler error , identity=[{}]", name, appContext.getConfig().getIdentity(),
						e);
			}
		}
	}

	public void inSchedule(String key, T value) {
		try {
			lock.tryLock();
			failStore.put(key, value);
			LOGGER.info("{} RetryScheduler, local files save success, identity=[{}], {}", name,
					appContext.getConfig().getIdentity(), JSON.toJSONString(value));
		} catch (FailStoreException e) {
			LOGGER.error("{} RetryScheduler in schedule error, identity=[{}]", name, e,
					appContext.getConfig().getIdentity());
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	/**
	 * 重试
	 */
	protected abstract boolean retry(List<T> list);

}
