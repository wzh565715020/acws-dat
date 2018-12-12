package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.admin.request.TaskQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

public interface QueueInterface {

    <T> PaginationRsp<T> pageSelect(TaskQueueReq request);

    boolean selectiveUpdate(TaskQueueReq request);

}
