package com.tyyd.framework.dat.core.registry;

import com.tyyd.framework.dat.core.cluster.Node;

import java.util.List;

public interface NotifyListener {

    void notify(NotifyEvent event, List<Node> nodes);

}
