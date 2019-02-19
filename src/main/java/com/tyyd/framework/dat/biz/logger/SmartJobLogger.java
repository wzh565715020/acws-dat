package com.tyyd.framework.dat.biz.logger;

import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.TaskLoggerRequest;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.spi.ServiceLoader;

import java.util.List;

/**
 * 内部根据用户参数决定是否采用延迟批量刷盘的策略,来提高吞吐量
 *
 */
public class SmartJobLogger implements TaskLogger {

    private TaskLogger delegate;

    public SmartJobLogger(AppContext appContext) {
        Config config = appContext.getConfig();
        JobLoggerFactory jobLoggerFactory = ServiceLoader.load(JobLoggerFactory.class, config);
        TaskLogger jobLogger = jobLoggerFactory.getJobLogger(config);
        if (config.getParameter(Constants.LAZY_TASK_LOGGER, false)) {
            this.delegate = new LazyJobLogger(appContext, jobLogger);
        } else {
            this.delegate = jobLogger;
        }
    }

    @Override
    public void log(TaskLogPo jobLogPo) {
        this.delegate.log(jobLogPo);
    }

    @Override
    public void log(List<TaskLogPo> jobLogPos) {
        this.delegate.log(jobLogPos);
    }

    @Override
    public PaginationRsp<TaskLogPo> search(TaskLoggerRequest request) {
        return this.delegate.search(request);
    }
}
