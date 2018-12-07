package com.tyyd.framework.dat.management.access.mysql;


import java.util.List;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.management.access.RshHandler;
import com.tyyd.framework.dat.management.access.face.BackendJVMThreadAccess;
import com.tyyd.framework.dat.management.monitor.access.domain.JVMThreadDataPo;
import com.tyyd.framework.dat.management.monitor.access.mysql.MysqlJVMThreadAccess;
import com.tyyd.framework.dat.management.request.JvmDataReq;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.WhereSql;

public class MysqlBackendJVMThreadAccess extends MysqlJVMThreadAccess implements BackendJVMThreadAccess {

    public MysqlBackendJVMThreadAccess(Config config) {
        super(config);
    }

    @Override
    public void delete(JvmDataReq request) {
        new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .doDelete();
    }

    @Override
    public List<JVMThreadDataPo> queryAvg(MDataPaginationReq request) {
        return new SelectSql(getSqlTemplate())
                .select()
                .columns("timestamp",
                        "AVG(daemon_thread_count) AS daemon_thread_count",
                        "AVG(thread_count) AS thread_count",
                        "AVG(total_started_thread_count) AS total_started_thread_count",
                        "AVG(dead_locked_thread_count) AS dead_locked_thread_count",
                        "AVG(process_cpu_time_rate) AS process_cpu_time_rate")
                .from()
                .table(getTableName())
                .whereSql(buildWhereSql(request))
                .groupBy(" timestamp ASC ")
                .limit(request.getStart(), request.getLimit())
                .list(RshHandler.JVM_THREAD_SUM_M_DATA_RSH);
    }

    public WhereSql buildWhereSql(JvmDataReq req) {
        return new WhereSql()
                .andOnNotEmpty("identity = ?", req.getIdentity())
                .andBetween("timestamp", req.getStartTime(), req.getEndTime());

    }

    public WhereSql buildWhereSql(MDataPaginationReq request) {
        return new WhereSql()
                .andOnNotNull("id = ?", request.getId())
                .andOnNotEmpty("identity = ?", request.getIdentity())
                .andOnNotEmpty("node_group = ?", request.getNodeGroup())
                .andBetween("timestamp", request.getStartTime(), request.getEndTime());
    }
}
