package com.tyyd.framework.dat.management.access;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.management.access.face.BackendJVMGCAccess;
import com.tyyd.framework.dat.management.access.face.BackendJVMMemoryAccess;
import com.tyyd.framework.dat.management.access.face.BackendJVMThreadAccess;
import com.tyyd.framework.dat.management.access.face.BackendJobClientMAccess;
import com.tyyd.framework.dat.management.access.face.BackendJobTrackerMAccess;
import com.tyyd.framework.dat.management.access.face.BackendNodeOnOfflineLogAccess;
import com.tyyd.framework.dat.management.access.face.BackendTaskTrackerMAccess;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

@SPI(key = SpiExtensionKey.ACCESS_DB, dftValue = "mysql")
public interface BackendAccessFactory {

    BackendJobTrackerMAccess getJobTrackerMAccess(Config config);

    BackendJobClientMAccess getBackendJobClientMAccess(Config config);

    BackendJVMGCAccess getBackendJVMGCAccess(Config config);

    BackendJVMMemoryAccess getBackendJVMMemoryAccess(Config config);

    BackendJVMThreadAccess getBackendJVMThreadAccess(Config config);

    BackendNodeOnOfflineLogAccess getBackendNodeOnOfflineLogAccess(Config config);

    BackendTaskTrackerMAccess getBackendTaskTrackerMAccess(Config config);
}
