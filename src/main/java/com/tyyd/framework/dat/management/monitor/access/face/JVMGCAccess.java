package com.tyyd.framework.dat.management.monitor.access.face;



import java.util.List;

import com.tyyd.framework.dat.management.monitor.access.domain.JVMGCDataPo;

/**
 * @author   on 9/28/15.
 */
public interface JVMGCAccess {

    void insert(List<JVMGCDataPo> pos);

}
