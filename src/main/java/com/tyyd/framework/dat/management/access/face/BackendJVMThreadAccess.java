package com.tyyd.framework.dat.management.access.face;


import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JVMThreadDataPo;
import com.tyyd.framework.dat.management.monitor.access.face.JVMThreadAccess;
import com.tyyd.framework.dat.management.request.JvmDataReq;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;

public interface BackendJVMThreadAccess extends JVMThreadAccess {

    void delete(JvmDataReq request);

    List<JVMThreadDataPo> queryAvg(MDataPaginationReq request);

}
