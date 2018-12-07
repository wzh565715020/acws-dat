package com.tyyd.framework.dat.management.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JobTrackerMDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JobTrackerMAccess;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;

public interface BackendJobTrackerMAccess extends JobTrackerMAccess {

    List<JobTrackerMDataPo> querySum(MDataPaginationReq request);

    void delete(MDataPaginationReq request);

    List<String> getJobTrackers();
}
