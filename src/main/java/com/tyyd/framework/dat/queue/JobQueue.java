package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.admin.request.JobQueueReq;
import com.tyyd.framework.dat.queue.domain.JobPo;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

/**
 * @author Robert HG (254963746@qq.com) on 6/6/15.
 */
public interface JobQueue {

    PaginationRsp<JobPo> pageSelect(JobQueueReq request);

    boolean selectiveUpdate(JobQueueReq request);

}
