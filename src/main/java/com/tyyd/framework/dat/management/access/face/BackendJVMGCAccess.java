package com.tyyd.framework.dat.management.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JVMGCDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JVMGCAccess;
import com.tyyd.framework.dat.management.request.JvmDataReq;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;

public interface BackendJVMGCAccess extends JVMGCAccess {

    void delete(JvmDataReq request);

    List<JVMGCDataPo> queryAvg(MDataPaginationReq request);
}
