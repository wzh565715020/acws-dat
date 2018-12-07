package com.tyyd.framework.dat.management.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.TaskTrackerMDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.TaskTrackerMAccess;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;
import com.tyyd.framework.dat.management.web.vo.NodeInfo;

public interface BackendTaskTrackerMAccess extends TaskTrackerMAccess{

    List<TaskTrackerMDataPo> querySum(MDataPaginationReq request);

    void delete(MDataPaginationReq request);

    List<NodeInfo> getTaskTrackers();
}
