package com.tyyd.framework.dat.management.monitor.access;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.spi.SPI;
import com.tyyd.framework.dat.management.monitor.access.face.JVMGCAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JVMMemoryAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JVMThreadAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JobClientMAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JobTrackerMAccess;
import com.tyyd.framework.dat.management.monitor.access.face.TaskTrackerMAccess;
import com.tyyd.framework.dat.core.spi.SpiExtensionKey;

@SPI(key = SpiExtensionKey.ACCESS_DB, dftValue = "mysql")
public interface MonitorAccessFactory {

    JobTrackerMAccess getJobTrackerMAccess(Config config);

    TaskTrackerMAccess getTaskTrackerMAccess(Config config);

    JVMGCAccess getJVMGCAccess(Config config);

    JVMMemoryAccess getJVMMemoryAccess(Config config);

    JVMThreadAccess getJVMThreadAccess(Config config);

    JobClientMAccess getJobClientMAccess(Config config);
}
