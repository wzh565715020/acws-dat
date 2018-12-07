package com.tyyd.framework.dat.management.access.face;

import java.util.List;

import com.tyyd.framework.dat.management.access.domain.NodeOnOfflineLog;
import com.tyyd.framework.dat.management.request.NodeOnOfflineLogPaginationReq;

public interface BackendNodeOnOfflineLogAccess {

    void insert(List<NodeOnOfflineLog> nodeOnOfflineLogs);

    List<NodeOnOfflineLog> select(NodeOnOfflineLogPaginationReq request);

    Long count(NodeOnOfflineLogPaginationReq request);

    void delete(NodeOnOfflineLogPaginationReq request);

}
