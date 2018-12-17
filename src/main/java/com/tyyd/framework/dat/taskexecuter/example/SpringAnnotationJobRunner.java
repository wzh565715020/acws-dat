package com.tyyd.framework.dat.taskexecuter.example;

import org.springframework.beans.factory.annotation.Autowired;

import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.logger.BizLogger;
import com.tyyd.framework.dat.taskexecuter.runner.DatLoggerFactory;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

public class SpringAnnotationJobRunner implements TaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAnnotationJobRunner.class);

    @Autowired
    SpringBean springBean;

    @Override
    public Result run(Task job) throws Throwable {
        try {
            Thread.sleep(1000L);

            springBean.hello();

            // TODO 业务逻辑
            LOGGER.info("我要执行：" + job);
            BizLogger bizLogger = DatLoggerFactory.getBizLogger();
            // 会发送到 LTS (JobTracker上)
            bizLogger.info("测试，业务日志啊啊啊啊啊");

        } catch (Exception e) {
            LOGGER.info("Run job failed!", e);
            return new Result(Action.EXECUTE_LATER, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了，哈哈");
    }

}
