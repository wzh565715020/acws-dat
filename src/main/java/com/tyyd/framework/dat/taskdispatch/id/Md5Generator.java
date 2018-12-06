package com.tyyd.framework.dat.taskdispatch.id;

import com.tyyd.framework.dat.core.commons.utils.Md5Encrypt;
import com.tyyd.framework.dat.queue.domain.JobPo;

public class Md5Generator implements IdGenerator{
    @Override
    public String generate(JobPo jobPo) {
        return Md5Encrypt.md5(jobPo.getTaskId() + jobPo.getSubmitNodeGroup() + jobPo.getGmtCreated());
    }
}
