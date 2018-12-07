package com.tyyd.framework.dat.management.monitor.access.face;



import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JVMThreadDataPo;

public interface JVMThreadAccess {

    void insert(List<JVMThreadDataPo> pos);

}
