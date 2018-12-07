package com.tyyd.framework.dat.management.monitor.access.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.management.monitor.access.MonitorAccessFactory;
import com.tyyd.framework.dat.management.monitor.access.face.JVMGCAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JVMMemoryAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JVMThreadAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JobClientMAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JobTrackerMAccess;
import com.tyyd.framework.dat.management.monitor.access.face.TaskTrackerMAccess;

public class MysqlMonitorAccessFactory implements MonitorAccessFactory {

    @Override
    public JobTrackerMAccess getJobTrackerMAccess(Config config) {
        return new MysqlJobTrackerMAccess(config);
    }

    @Override
    public TaskTrackerMAccess getTaskTrackerMAccess(Config config) {
        return new MysqlTaskTrackerMAccess(config);
    }

    @Override
    public JVMGCAccess getJVMGCAccess(Config config) {
        return new MysqlJVMGCAccess(config);
    }

    @Override
    public JVMMemoryAccess getJVMMemoryAccess(Config config) {
        return new MysqlJVMMemoryAccess(config);
    }

    @Override
    public JVMThreadAccess getJVMThreadAccess(Config config) {
        return new MysqlJVMThreadAccess(config);
    }

    @Override
    public JobClientMAccess getJobClientMAccess(Config config) {
        return new MysqlJobClientMAccess(config);
    }

}
