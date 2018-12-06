package com.tyyd.framework.dat.core.failstore.mapdb;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.Job;
import com.tyyd.framework.dat.core.domain.Pair;
import com.tyyd.framework.dat.core.failstore.FailStore;
import com.tyyd.framework.dat.core.failstore.FailStoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 11/10/15.
 */
public class MapdbFailStoreTest {

    private String key = "23412x";
    FailStore failStore;

    @Before
    public void setup() throws FailStoreException {
        Config config = new Config();
        config.setIdentity("testIdentity");
        config.setDataPath(Constants.USER_HOME);
        config.setNodeGroup("test");
        config.setNodeType(NodeType.JOB_CLIENT);
        failStore = new MapdbFailStoreFactory().getFailStore(config, config.getFailStorePath());
        failStore.open();
    }

    @Test
    public void put() throws FailStoreException {
        Job job = new Job();
        job.setTaskId("2131232");
        for (int i = 0; i < 100; i++) {
            failStore.put(key + "" + i, job);
        }
        System.out.println("这里debug测试多线程");
        failStore.close();
    }

    @Test
    public void fetchTop() throws FailStoreException {
        List<Pair<String, Job>> pairs = failStore.fetchTop(5, Job.class);
        if (CollectionUtils.isNotEmpty(pairs)) {
            for (Pair<String, Job> pair : pairs) {
                System.out.println(JSON.toJSONString(pair));
            }
        }
    }
}