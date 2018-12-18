package com.tyyd.framework.dat.queue.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CharacterUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.queue.PoolQueueInterface;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.queue.mysql.support.RshHolder;
import com.tyyd.framework.dat.store.jdbc.JdbcAbstractAccess;
import com.tyyd.framework.dat.store.jdbc.builder.InsertSql;
import com.tyyd.framework.dat.store.jdbc.builder.OrderByType;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.UpdateSql;
import com.tyyd.framework.dat.store.jdbc.exception.JdbcException;
import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

import java.util.List;

public abstract class AbstractMysqlPoolQueue extends JdbcAbstractAccess implements PoolQueueInterface {

	public AbstractMysqlPoolQueue(Config config) {
		super(config);
	}

	protected boolean add(String tableName, PoolPo poolPo) {
		return new InsertSql(getSqlTemplate()).insert(tableName)
				.columns("pool_id", "pool_name", "max_count","available_count", "task_ids", "memo", "create_date", "update_date",
						"create_userid", "update_userid")
				.values(poolPo.getPoolId(), poolPo.getPoolName(), poolPo.getMaxCount(),poolPo.getAvailableCount(), poolPo.getTaskIds(),
						poolPo.getMemo(), poolPo.getCreateDate(), poolPo.getUpdateDate(), poolPo.getCreateUserId(),
						poolPo.getUpdateUserId())
				.doInsert() == 1;
	}
	public PaginationRsp<PoolPo> pageSelect(PoolQueueReq request) {

		PaginationRsp<PoolPo> response = new PaginationRsp<PoolPo>();
		Long results = new SelectSql(getSqlTemplate()).select().columns("count(1)").from().table(getTableName())
				.single();
		response.setResults(results.intValue());

		if (results > 0) {
			List<PoolPo> poolPos = new SelectSql(getSqlTemplate()).select().all().from().table(getTableName()).orderBy()
					.column(CharacterUtils.camelCase2Underscore(request.getField()),
							OrderByType.convert(request.getDirection()))
					.limit(request.getStart(), request.getLimit()).list(RshHolder.POOL_PO_LIST_RSH);
			response.setRows(poolPos);
		}
		return response;
	}

	protected abstract String getTableName();
	
	public boolean selectiveUpdate(PoolQueueReq request) {

		if (StringUtils.isEmpty(request.getPoolId())) {
			throw new JdbcException("Only allow update by poolId");
		}
		return new UpdateSql(getSqlTemplate()).update().table(getTableName())
				.setOnNotNull("pool_name", request.getPoolName())
				.setOnNotNull("max_count", request.getMaxCount())
				.setOnNotNull("available_count", request.getAvailableCount())
				.setOnNotNull("task_ids", request.getTaskIds())
				.setOnNotNull("memo", request.getMemo())
				.setOnNotNull("update_date", request.getUpdateDate())
				.setOnNotNull("update_userid", request.getUpdateUserId()).where("pool_id = ?", request.getPoolId())
				.doUpdate() == 1;
	}
}
