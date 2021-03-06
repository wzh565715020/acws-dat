package com.tyyd.framework.dat.core.constant;


import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 *         一些配置常量
 */
public interface Constants {

    // 可用的处理器个数
    int AVAILABLE_PROCESSOR = Runtime.getRuntime().availableProcessors();

    String OS_NAME = System.getProperty("os.name");

    String USER_HOME = System.getProperty("user.home");

    String LINE_SEPARATOR = System.getProperty("line.separator");

    int TASK_DISPATCH_DEFAULT_LISTEN_PORT = 35001;
    
    int TASK_EXECUTER_DEFAULT_LISTEN_PORT = 35002;

    // 默认集群名字
    String DEFAULT_CLUSTER_NAME = "DEFAULT_CLUSTER";

    String CHARSET = "UTF-8";

    int DEFAULT_TIMEOUT = 1000;

    String TIMEOUT_KEY = "timeout";

    String SESSION_TIMEOUT_KEY = "session";

    int DEFAULT_SESSION_TIMEOUT = 60 * 1000;

    String REGISTER = "register";

    String UNREGISTER = "unregister";

    String SUBSCRIBE = "subscribe";

    String UNSUBSCRIBE = "unsubscribe";

    int DEFAULT_BUFFER_SIZE = 16 * 1024 * 1024;
    /**
     * 注册中心失败事件重试事件
     */
    String REGISTRY_RETRY_PERIOD_KEY = "retry.period";

    /**
     * 重试周期
     */
    int DEFAULT_REGISTRY_RETRY_PERIOD = 5 * 1000;

    Pattern COMMA_SPLIT_PATTERN = Pattern.compile("\\s*[,]+\\s*");

    /**
     * 注册中心自动重连时间
     */
    String REGISTRY_RECONNECT_PERIOD_KEY = "reconnect.period";

    int DEFAULT_REGISTRY_RECONNECT_PERIOD = 3 * 1000;

    // 客户端提交并发请求size
    String TASK_SUBMIT_MAX_QPS = "task.submit.maxQPS";
    int DEFAULT_TASK_SUBMIT_MAX_QPS = 500;

    String PROCESSOR_THREAD = "task.processor.thread";
    int DEFAULT_PROCESSOR_THREAD = 32 + AVAILABLE_PROCESSOR * 5;

    int LATCH_TIMEOUT_MILLIS = 10 * 60 * 1000;      // 10分钟

    // 任务最多重试次数
    String TASK_MAX_RETRY_TIMES = "task.max.retry.times";
    int DEFAULT_TASK_MAX_RETRY_TIMES = 10;

    Charset UTF_8 = Charset.forName("UTF-8");

    String TASK_PUSH_FREQUENCY = "task.push.frequency";
    
    String TASK_PUSH_NODE_GROUP = "task.push.node.group";
    
    
    int DEFAULT_TASK_PUSH_FREQUENCY = 10;

    // TaskTracker 离线(网络隔离)时间 10s，超过10s，自动停止当前执行任务
    long DEFAULT_TASK_EXECUTER_OFFLINE_LIMIT_MILLIS = 60 * 60 * 1000;
    // 当TaskTracker离线超过了这个时间,那么就会进入自杀流程,停止当前所有线程
//    String TASK_TRACKER_OFFLINE_LIMIT_MILLIS = "tasktracker.offline.limit.millis";
    // TaskTracker超过一定时间断线JobTracker，自动停止当前的所有任务
    String TASK_EXECUTER_STOP_WORKING_ENABLE = "taskdispatcher.stop.working.enable";

    String ADMIN_ID_PREFIX = "DAT_admin_";

    // 是否延迟批量刷盘日志, 如果启用，采用队列的方式批量将日志刷盘(在应用关闭的时候，可能会造成日志丢失)
    String LAZY_TASK_LOGGER = "lazy.task.logger";
    // 延迟批量刷盘日志 内存中的最大日志量阀值
    String LAZY_TASK_LOGGER_MEM_SIZE = "lazy.task.logger.mem.size";
    // 延迟批量刷盘日志 检查频率
    String LAZY_TASK_LOGGER_CHECK_PERIOD = "lazy.task.logger.check.period";

    String LAZY_TASK_LOGGER_BATCH_FLUSH_SIZE = "lazy.task.logger.batch.flush.size";
    String LAZY_TASK_LOGGER_OVERFLOW_SIZE = "lazy.task.logger.overflow.size";

    String ADAPTIVE = "adaptive";

    String MACHINE_RES_ENOUGH = "__DAT.INNER.MACHINE.RES.ENOUGH";

    String LB_MEMORY_USED_RATE_MAX = "lb.memoryUsedRate.max";

    String LB_CPU_USED_RATE_MAX = "lb.cpuUsedRate.max";

    String LB_MACHINE_RES_CHECK_ENABLE = "lb.machine.res.check.enable";

    String QUARTZ_FIRST_FIRE_TIME = "__DAT_Quartz_First_Fire_Time";
}
