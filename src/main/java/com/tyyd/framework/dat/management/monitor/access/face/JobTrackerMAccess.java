package com.tyyd.framework.dat.management.monitor.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JobTrackerMDataPo;

public interface JobTrackerMAccess {

    void insert(List<JobTrackerMDataPo> jobTrackerMDataPos);

}
