package com.tyyd.framework.dat.biz.logger;

import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.JobLoggerRequest;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

import java.util.List;

/**
 * 执行任务日志记录器
 *
 * @author Robert HG (254963746@qq.com) on 3/24/15.
 */
public interface JobLogger {

    public void log(JobLogPo jobLogPo);

    public void log(List<JobLogPo> jobLogPos);

    public PaginationRsp<JobLogPo> search(JobLoggerRequest request);
}