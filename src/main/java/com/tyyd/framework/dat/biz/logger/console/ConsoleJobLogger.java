package com.tyyd.framework.dat.biz.logger.console;

import com.tyyd.framework.dat.biz.logger.TaskLogger;
import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.TaskLoggerRequest;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;

import java.util.List;

public class ConsoleJobLogger implements TaskLogger {

    private Logger LOGGER = LoggerFactory.getLogger(ConsoleJobLogger.class.getSimpleName());

    @Override
    public void log(TaskLogPo jobLogPo) {
        LOGGER.info(JSON.toJSONString(jobLogPo));
    }

    @Override
    public void log(List<TaskLogPo> jobLogPos) {
        for (TaskLogPo jobLogPo : jobLogPos) {
            log(jobLogPo);
        }
    }

    @Override
    public PaginationRsp<TaskLogPo> search(TaskLoggerRequest request) {
        throw new UnsupportedOperationException("Console logger dose not support this operation!");
    }

}
