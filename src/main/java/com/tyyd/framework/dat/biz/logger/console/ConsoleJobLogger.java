package com.tyyd.framework.dat.biz.logger.console;

import com.tyyd.framework.dat.biz.logger.JobLogger;
import com.tyyd.framework.dat.biz.logger.domain.JobLogPo;
import com.tyyd.framework.dat.biz.logger.domain.JobLoggerRequest;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public class ConsoleJobLogger implements JobLogger {

    private Logger LOGGER = LoggerFactory.getLogger(ConsoleJobLogger.class.getSimpleName());

    @Override
    public void log(JobLogPo jobLogPo) {
        LOGGER.info(JSON.toJSONString(jobLogPo));
    }

    @Override
    public void log(List<JobLogPo> jobLogPos) {
        for (JobLogPo jobLogPo : jobLogPos) {
            log(jobLogPo);
        }
    }

    @Override
    public PaginationRsp<JobLogPo> search(JobLoggerRequest request) {
        throw new UnsupportedOperationException("Console logger dose not support this operation!");
    }

}
