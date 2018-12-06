package com.tyyd.framework.dat.queue;

import com.tyyd.framework.dat.queue.domain.JobPo;

/**
 * @author Robert HG (254963746@qq.com) on 8/14/15.
 */
public interface PreLoader {

    public JobPo take(String taskTrackerNodeGroup, String taskTrackerIdentity);

    /**
     * 如果taskTrackerNodeGroup为空，那么load所有的
     */
    public void load(String taskTrackerNodeGroup);
}