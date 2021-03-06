package com.tyyd.framework.dat.core.failstore.rocksdb;

import com.tyyd.framework.dat.core.cluster.Config;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.domain.Task;
import com.tyyd.framework.dat.core.domain.Pair;
import com.tyyd.framework.dat.core.failstore.FailStore;
import com.tyyd.framework.dat.core.failstore.FailStoreException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Robert HG (254963746@qq.com) on 5/27/15.
 */
public class RocksdbFailStoreTest {

    FailStore failStore;

    private String key = "23412x";

    @Before
    public void setup() throws FailStoreException {
        Config config = new Config();
        config.setIdentity("testIdentity");
        config.setDataPath(Constants.USER_HOME);
        config.setNodeType(NodeType.TASK_CLIENT);
        failStore = new RocksdbFailStoreFactory().getFailStore(config, config.getFailStorePath());
        failStore.open();
    }

    @Test
    public void put() throws FailStoreException {
        Task task = new Task();
        task.setTaskId("2131232");
        for (int i = 0; i < 100; i++) {
            failStore.put(key + "" + i, task);
        }
        System.out.println("这里debug测试多线程");
        failStore.close();
    }

    @Test
    public void fetchTop() throws FailStoreException {
        List<Pair<String, Task>> pairs = failStore.fetchTop(5, Task.class);
        if (CollectionUtils.isNotEmpty(pairs)) {
            for (Pair<String, Task> pair : pairs) {
                System.out.println(JSON.toJSONString(pair));
            }
        }
    }
}