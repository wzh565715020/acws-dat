package com.tyyd.framework.dat.management.monitor.access.mysql;


import java.util.List;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.management.monitor.access.domain.JVMGCDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JVMGCAccess;
import com.tyyd.framework.dat.store.jdbc.builder.InsertSql;

public class MysqlJVMGCAccess extends MysqlAbstractJdbcAccess implements JVMGCAccess {

    public MysqlJVMGCAccess(Config config) {
        super(config);
    }

    @Override
    public void insert(List<JVMGCDataPo> jvmGCDataPos) {
        if (CollectionUtils.isEmpty(jvmGCDataPos)) {
            return;
        }

        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("gmt_created",
                        "identity",
                        "timestamp",
                        "node_type",
                        "node_group",
                        "young_gc_collection_count",
                        "young_gc_collection_time",
                        "full_gc_collection_count",
                        "full_gc_collection_time",
                        "span_young_gc_collection_count",
                        "span_young_gc_collection_time",
                        "span_full_gc_collection_count",
                        "span_full_gc_collection_time");

        for (JVMGCDataPo po : jvmGCDataPos) {
            insertSql.values(
                    po.getGmtCreated(),
                    po.getIdentity(),
                    po.getTimestamp(),
                    po.getNodeType().name(),
                    po.getNodeGroup(),
                    po.getYoungGCCollectionCount(),
                    po.getYoungGCCollectionTime(),
                    po.getFullGCCollectionCount(),
                    po.getFullGCCollectionTime(),
                    po.getSpanYoungGCCollectionCount(),
                    po.getSpanYoungGCCollectionTime(),
                    po.getSpanFullGCCollectionCount(),
                    po.getSpanFullGCCollectionTime()
            );
        }

        insertSql.doBatchInsert();

    }

    @Override
    protected String getTableName() {
        return "lts_admin_jvm_gc";
    }

}
