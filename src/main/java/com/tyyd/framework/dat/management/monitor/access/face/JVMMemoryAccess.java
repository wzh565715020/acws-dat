package com.tyyd.framework.dat.management.monitor.access.face;



import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JVMMemoryDataPo;

public interface JVMMemoryAccess {

    void insert(List<JVMMemoryDataPo> pos);

}
