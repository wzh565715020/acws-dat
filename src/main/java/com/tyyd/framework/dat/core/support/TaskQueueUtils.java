package com.tyyd.framework.dat.core.support;


public class TaskQueueUtils {

    private TaskQueueUtils() {
    }

    /**
     * 在数据库中就是表名, taskTrackerNodeGroup 是 TaskTracker的 nodeGroup
     */
    public static String getExecutableQueueName() {
        return EXECUTABLE_TASK_QUEUE;
    }

    /**
     * 在数据库中就是表名, jobClientNodeGroup 是 JobClient 的 nodeGroup
     */
    public static String getFeedbackQueueName() {
        return "dat_feedback_task_queue";
    }

    public static final String EXECUTABLE_TASK_QUEUE =  "dat_executable_task_queue";
    
    public static final String TASK_QUEUE = "dat_task_queue";

    public static final String EXECUTING_TASK_QUEUE = "dat_executing_task_queue";

	public static final String SUSPEND_JOB_QUEUE = "dat_suspend_task_queue";
	
	public static final String TASK_POOL = "dat_task_pool";
}
