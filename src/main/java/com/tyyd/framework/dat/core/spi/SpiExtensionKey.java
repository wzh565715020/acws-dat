package com.tyyd.framework.dat.core.spi;

public interface SpiExtensionKey {

    String FAIL_STORE = "job.fail.store";

    String LOADBALANCE = "loadbalance";

    String EVENT_CENTER = "event.center";

    String REMOTING = "lts.remoting";

    String REMOTING_SERIALIZABLE_DFT = "lts.remoting.serializable.default";

    String ZK_CLIENT_KEY = "zk.client";

    String JOB_ID_GENERATOR = "id.generator";

    String JOB_LOGGER = "job.logger";

    String LTS_LOGGER = "lts.logger";

    String JOB_QUEUE = "job.queue";

    String LTS_JSON = "lts.json";

    String ACCESS_DB = "lts.admin.access.db";
}
