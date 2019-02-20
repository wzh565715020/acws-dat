package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.support.TaskQueueUtils;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.queue.AbstractPreLoader;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.SqlTemplate;
import com.tyyd.framework.dat.store.jdbc.SqlTemplateFactory;
import com.tyyd.framework.dat.store.jdbc.builder.OrderByType;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;

import java.util.List;

public class MysqlPreLoader extends AbstractPreLoader {

	private static final Logger LOGGER = LoggerFactory.getLogger(MysqlPreLoader.class);
	
	private SqlTemplate sqlTemplate;

	public MysqlPreLoader(AppContext appContext, String poolId) {
		super(appContext, poolId);
		this.sqlTemplate = SqlTemplateFactory.create();
	}

	@Override
	public boolean lockTask(String id, String taskTrackerIdentity) {
		return new UpdateSql(sqlTemplate).update().table(getTableName()).set("is_running", 1)
				.set("task_execute_node", taskTrackerIdentity).set("update_date", SystemClock.now()).where("id = ?", id)
				.and("is_running = ?", 0).doUpdate() == 1;
	}

	@Override
	protected List<TaskPo> load(String poolId, int loadSize) {
		return new SelectSql(sqlTemplate).select().all().from().table(getTableName()).where("is_running = ?", 0)
				.and("pool_id = ?", poolId).and("trigger_time< ?", SystemClock.now()).orderBy()
				.column("trigger_time", OrderByType.ASC).limit(0, loadSize).list(RshHolder.TASK_PO_LIST_RSH);
	}

	private String getTableName() {
		return TaskQueueUtils.getExecutableQueueName();
	}
}
