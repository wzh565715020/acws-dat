package com.tyyd.framework.dat.management.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JobClientMDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JobClientMAccess;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;
import com.tyyd.framework.dat.management.web.vo.NodeInfo;

public interface BackendJobClientMAccess extends JobClientMAccess {

    void delete(MDataPaginationReq request);

    List<JobClientMDataPo> querySum(MDataPaginationReq request);

    List<NodeInfo> getJobClients();
}
