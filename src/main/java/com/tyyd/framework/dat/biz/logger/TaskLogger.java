package com.tyyd.framework.dat.biz.logger;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.JobLoggerRequest;
import com.tyyd.framework.dat.admin.response.PaginationRsp;

import java.util.List;

/**
 * 执行任务日志记录器
 *
 * @author   on 3/24/15.
 */
public interface TaskLogger {

    public void log(TaskLogPo jobLogPo);

    public void log(List<TaskLogPo> jobLogPos);

    public PaginationRsp<TaskLogPo> search(JobLoggerRequest request);
}