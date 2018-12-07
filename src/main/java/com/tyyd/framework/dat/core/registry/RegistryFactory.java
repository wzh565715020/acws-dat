package com.tyyd.framework.dat.core.registry;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.registry.redis.RedisRegistry;
import com.tyyd.framework.dat.core.registry.zookeeper.ZookeeperRegistry;

public class RegistryFactory {

    public static Registry getRegistry(AppContext appContext) {

        String address = appContext.getConfig().getRegistryAddress();
        if (StringUtils.isEmpty(address)) {
            throw new IllegalArgumentException("address is null！");
        }
        if (address.startsWith("zookeeper://")) {
            return new ZookeeperRegistry(appContext);
        } else if (address.startsWith("redis://")) {
            return new RedisRegistry(appContext);
        } else if (address.startsWith("multicast://")) {
//            return new MulticastRegistry(config);
        }
        throw new IllegalArgumentException("illegal address protocol");
    }

}
