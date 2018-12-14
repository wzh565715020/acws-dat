package com.tyyd.framework.dat.core.spi;

public interface SpiExtensionKey {

    String FAIL_STORE = "task.fail.store";

    String LOADBALANCE = "loadbalance";

    String EVENT_CENTER = "event.center";

    String REMOTING = "dat.remoting";

    String REMOTING_SERIALIZABLE_DFT = "dat.remoting.serializable.default";

    String ZK_CLIENT_KEY = "zk.client";

    String TASK_ID_GENERATOR = "id.generator";

    String TASK_LOGGER = "task.logger";

    String DAT_LOGGER = "dat.logger";

    String TASK_QUEUE = "task.queue";

    String DAT_JSON = "dat.json";

    String ACCESS_DB = "dat.admin.access.db";
}
