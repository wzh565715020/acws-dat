package com.tyyd.framework.dat.management.monitor.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JobClientMDataPo;

/**
 * @author Robert HG (254963746@qq.com) on 3/12/16.
 */
public interface JobClientMAccess {

    void insert(List<JobClientMDataPo> jobTrackerMDataPos);

}
