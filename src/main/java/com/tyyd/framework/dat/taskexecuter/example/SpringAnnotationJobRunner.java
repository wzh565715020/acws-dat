package com.tyyd.framework.dat.taskexecuter.example;


import com.tyyd.framework.dat.core.domain.Action;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.taskexecuter.Result;
import com.tyyd.framework.dat.taskexecuter.runner.TaskRunner;

public class SpringAnnotationJobRunner implements TaskRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringAnnotationJobRunner.class);

    @Override
    public Result run(Task task) throws Throwable {
        try {
            Thread.sleep(1000L);

            System.out.println("我是SpringBean，我执行了");

            LOGGER.info("我要执行：" + task);
            Thread.sleep(60000L);
            LOGGER.info("执行完成：" + task);
        } catch (Exception e) {
            LOGGER.info("Run task failed!", e);
            return new Result(Action.EXECUTE_LATER, e.getMessage());
        }
        return new Result(Action.EXECUTE_SUCCESS, "执行成功了");
    }

}
