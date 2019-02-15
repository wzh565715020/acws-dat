package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.queue.domain.PoolPo;

public interface PoolQueueInterface {

    PaginationRsp<PoolPo> pageSelect(PoolQueueReq request);

    boolean updateByPoolId(PoolQueueReq request);
    
}
