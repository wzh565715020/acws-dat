package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.admin.request.TaskQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.queue.domain.TaskPo;

public interface TaskQueueInterface {

    PaginationRsp<TaskPo> pageSelect(TaskQueueReq request);

    boolean selectiveUpdate(TaskQueueReq request);

}
