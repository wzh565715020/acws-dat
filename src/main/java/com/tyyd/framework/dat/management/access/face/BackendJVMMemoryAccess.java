package com.tyyd.framework.dat.management.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JVMMemoryDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JVMMemoryAccess;
import com.tyyd.framework.dat.management.request.JvmDataReq;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;

public interface BackendJVMMemoryAccess extends JVMMemoryAccess{

    void delete(JvmDataReq request);

    List<JVMMemoryDataPo> queryAvg(MDataPaginationReq request);
}
