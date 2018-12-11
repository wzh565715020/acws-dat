package com.tyyd.framework.dat.management.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.domain.NodeGroupGetReq;
import com.tyyd.framework.dat.management.access.domain.NodeOnOfflineLog;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.cluster.BackendRegistrySrv;
import com.tyyd.framework.dat.management.request.NodeGroupRequest;
import com.tyyd.framework.dat.management.request.NodeOnOfflineLogPaginationReq;
import com.tyyd.framework.dat.management.request.NodePaginationReq;
import com.tyyd.framework.dat.management.web.AbstractMVC;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;
import com.tyyd.framework.dat.queue.domain.NodeGroupPo;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/node")
public class NodeApi extends AbstractMVC {

    @Autowired
    private BackendRegistrySrv backendRegistrySrv;
    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("node-list-get")
    public RestfulResponse getNodeList(NodePaginationReq request) {
        RestfulResponse response = new RestfulResponse();

        List<Node> nodes = backendRegistrySrv.getOnlineNodes(request);

        response.setSuccess(true);
        response.setResults(CollectionUtils.sizeOf(nodes));
        response.setRows(nodes);

        return response;
    }

    @RequestMapping("registry-re-subscribe")
    public RestfulResponse reSubscribe() {
        RestfulResponse response = new RestfulResponse();

        backendRegistrySrv.reSubscribe();

        response.setSuccess(true);
        return response;
    }

    @RequestMapping("node-group-get")
    public RestfulResponse getNodeGroup(NodeGroupRequest request) {
        RestfulResponse response = new RestfulResponse();
        NodeGroupGetReq nodeGroupGetReq = new NodeGroupGetReq();
        nodeGroupGetReq.setNodeGroup(request.getNodeGroup());
        nodeGroupGetReq.setNodeType(request.getNodeType());
        PaginationRsp<NodeGroupPo> paginationRsp = appContext.getNodeGroupStore().getNodeGroup(nodeGroupGetReq);

        response.setResults(paginationRsp.getResults());
        response.setRows(paginationRsp.getRows());
        response.setSuccess(true);
        return response;
    }


    @RequestMapping("node-onoffline-log-get")
    public RestfulResponse delNodeGroup(NodeOnOfflineLogPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        Long results = appContext.getBackendNodeOnOfflineLogAccess().count(request);
        response.setResults(results.intValue());
        if (results > 0) {
            List<NodeOnOfflineLog> rows = appContext.getBackendNodeOnOfflineLogAccess().select(request);
            response.setRows(rows);
        } else {
            response.setRows(new ArrayList<Object>(0));
        }
        response.setSuccess(true);
        return response;
    }
}
