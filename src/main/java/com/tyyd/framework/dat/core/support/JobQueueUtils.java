package com.tyyd.framework.dat.core.support;


/**
 * @author Robert HG (254963746@qq.com) on 5/28/15.
 */
public class JobQueueUtils {

    private JobQueueUtils() {
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
    public static String getFeedbackQueueName(String jobClientNodeGroup) {
        return "dat_feedback_job_queue";
    }

    public static final String EXECUTABLE_TASK_QUEUE =  "dat_executable_job_queue";
    
    public static final String TASK_QUEUE = "dat_task_queue";

    public static final String EXECUTING_JOB_QUEUE = "dat_executing_job_queue";

    public static final String NODE_GROUP_STORE = "dat_node_group_store";

	public static final String SUSPEND_JOB_QUEUE = "dat_suspend_job_queue";
}
