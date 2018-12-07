package com.tyyd.framework.dat.management.monitor.access.mysql;

import java.util.List;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.management.monitor.access.domain.JVMMemoryDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JVMMemoryAccess;
import com.tyyd.framework.dat.store.jdbc.builder.InsertSql;

public class MysqlJVMMemoryAccess extends MysqlAbstractJdbcAccess implements JVMMemoryAccess {

    public MysqlJVMMemoryAccess(Config config) {
        super(config);
    }

    @Override
    public void insert(List<JVMMemoryDataPo> jvmMemoryDataPos) {

        if (CollectionUtils.isEmpty(jvmMemoryDataPos)) {
            return;
        }

        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("gmt_created",
                        "identity",
                        "timestamp",
                        "node_type",
                        "node_group",
                        "heap_memory_committed",
                        "heap_memory_init",
                        "heap_memory_max",
                        "heap_memory_used",
                        "non_heap_memory_committed",
                        "non_heap_memory_init",
                        "non_heap_memory_max",
                        "non_heap_memory_used",
                        "perm_gen_committed",
                        "perm_gen_init",
                        "perm_gen_max",
                        "perm_gen_used",
                        "old_gen_committed",
                        "old_gen_init",
                        "old_gen_max",
                        "old_gen_used",
                        "eden_space_committed",
                        "eden_space_init",
                        "eden_space_max",
                        "eden_space_used",
                        "survivor_committed",
                        "survivor_init",
                        "survivor_max",
                        "survivor_used");
        for (JVMMemoryDataPo po : jvmMemoryDataPos) {
            insertSql.values(
                    po.getGmtCreated(),
                    po.getIdentity(),
                    po.getTimestamp(),
                    po.getNodeType().name(),
                    po.getNodeGroup(),
                    po.getHeapMemoryCommitted(),
                    po.getHeapMemoryInit(),
                    po.getHeapMemoryMax(),
                    po.getHeapMemoryUsed(),
                    po.getNonHeapMemoryCommitted(),
                    po.getNonHeapMemoryInit(),
                    po.getNonHeapMemoryMax(),
                    po.getNonHeapMemoryUsed(),
                    po.getPermGenCommitted(),
                    po.getPermGenInit(),
                    po.getPermGenMax(),
                    po.getPermGenUsed(),
                    po.getOldGenCommitted(),
                    po.getOldGenInit(),
                    po.getOldGenMax(),
                    po.getOldGenUsed(),
                    po.getEdenSpaceCommitted(),
                    po.getEdenSpaceInit(),
                    po.getEdenSpaceMax(),
                    po.getEdenSpaceUsed(),
                    po.getSurvivorCommitted(),
                    po.getSurvivorInit(),
                    po.getSurvivorMax(),
                    po.getSurvivorUsed()
            );
        }

        insertSql.doBatchInsert();

    }

    @Override
    protected String getTableName() {
        return "lts_admin_jvm_memory";
    }

}
