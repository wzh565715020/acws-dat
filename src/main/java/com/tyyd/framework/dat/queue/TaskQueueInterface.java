package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.admin.request.JobQueueReq;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

public interface TaskQueueInterface {

    PaginationRsp<TaskPo> pageSelect(JobQueueReq request);

    boolean selectiveUpdate(JobQueueReq request);

}
