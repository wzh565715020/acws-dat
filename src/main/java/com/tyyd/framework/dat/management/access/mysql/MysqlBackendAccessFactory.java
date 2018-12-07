package com.tyyd.framework.dat.management.access.mysql;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.management.access.BackendAccessFactory;
import com.tyyd.framework.dat.management.access.face.BackendJVMGCAccess;
import com.tyyd.framework.dat.management.access.face.BackendJVMMemoryAccess;
import com.tyyd.framework.dat.management.access.face.BackendJVMThreadAccess;
import com.tyyd.framework.dat.management.access.face.BackendJobClientMAccess;
import com.tyyd.framework.dat.management.access.face.BackendJobTrackerMAccess;
import com.tyyd.framework.dat.management.access.face.BackendNodeOnOfflineLogAccess;
import com.tyyd.framework.dat.management.access.face.BackendTaskTrackerMAccess;

public class MysqlBackendAccessFactory implements BackendAccessFactory {
    @Override
    public BackendJobTrackerMAccess getJobTrackerMAccess(Config config) {
        return new MysqlBackendJobTrackerMAccess(config);
    }

    @Override
    public BackendJobClientMAccess getBackendJobClientMAccess(Config config) {
        return new MysqlBackendJobClientMAccess(config);
    }

    @Override
    public BackendJVMGCAccess getBackendJVMGCAccess(Config config) {
        return new MysqlBackendJVMGCAccess(config);
    }

    @Override
    public BackendJVMMemoryAccess getBackendJVMMemoryAccess(Config config) {
        return new MysqlBackendJVMMemoryAccess(config);
    }

    @Override
    public BackendJVMThreadAccess getBackendJVMThreadAccess(Config config) {
        return new MysqlBackendJVMThreadAccess(config);
    }

    @Override
    public BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess(Config config) {
        return new MysqlBackendNodeOnOfflineLogAccess(config);
    }

    @Override
    public BackendTaskTrackerMAccess getBackendTaskTrackerMAccess(Config config) {
        return new MysqlBackendTaskTrackerMAccess(config);
    }
}
