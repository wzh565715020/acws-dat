package com.tyyd.framework.dat.queue;


import com.tyyd.framework.dat.queue.domain.JobFeedbackPo;

import java.util.List;

/**
 * 这个是用来保存反馈客户端失败的任务结果队列
 *
 * @author Robert HG (254963746@qq.com) on 3/27/15.
 */
public interface TaskFeedbackQueue {

    /**
     * 入队列
     */
    public boolean add(List<JobFeedbackPo> jobFeedbackPos);

    /**
     * 出队列
     */
    public boolean remove(String jobClientNodeGroup, String id);

    /**
     * 队列的大小
     */
    public long getCount(String jobClientNodeGroup);

    /**
     * 取top几个
     */
    public List<JobFeedbackPo> fetchTop(String jobClientNodeGroup, int top);

}
