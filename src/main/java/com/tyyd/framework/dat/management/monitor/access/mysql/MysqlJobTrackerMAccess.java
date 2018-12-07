package com.tyyd.framework.dat.management.monitor.access.mysql;


import java.util.List;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.management.monitor.access.domain.JobTrackerMDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JobTrackerMAccess;
import com.tyyd.framework.dat.store.jdbc.builder.InsertSql;

public class MysqlJobTrackerMAccess extends MysqlAbstractJdbcAccess implements JobTrackerMAccess {

    public MysqlJobTrackerMAccess(Config config) {
        super(config);
    }

    @Override
    protected String getTableName() {
        return "lts_admin_job_tracker_monitor_data";
    }

    @Override
    public void insert(List<JobTrackerMDataPo> jobTrackerMDataPos) {

        InsertSql insertSql = new InsertSql(getSqlTemplate())
                .insert(getTableName())
                .columns("gmt_created",
                        "identity",
                        "timestamp",
                        "receive_job_num",
                        "push_job_num",
                        "exe_success_num",
                        "exe_failed_num",
                        "exe_later_num",
                        "exe_exception_num",
                        "fix_executing_job_num");

        for (JobTrackerMDataPo jobTrackerMDataPo : jobTrackerMDataPos) {
            insertSql.values(
                    jobTrackerMDataPo.getGmtCreated(),
                    jobTrackerMDataPo.getIdentity(),
                    jobTrackerMDataPo.getTimestamp(),
                    jobTrackerMDataPo.getReceiveJobNum(),
                    jobTrackerMDataPo.getPushJobNum(),
                    jobTrackerMDataPo.getExeSuccessNum(),
                    jobTrackerMDataPo.getExeFailedNum(),
                    jobTrackerMDataPo.getExeLaterNum(),
                    jobTrackerMDataPo.getExeExceptionNum(),
                    jobTrackerMDataPo.getFixExecutingJobNum()
            );
        }
        insertSql.doBatchInsert();
    }

}
