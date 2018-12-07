package com.tyyd.framework.dat.management.web.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.monitor.access.domain.MDataPo;
import com.tyyd.framework.dat.management.request.MDataPaginationReq;
import com.tyyd.framework.dat.management.web.AbstractMVC;
import com.tyyd.framework.dat.management.web.vo.RestfulResponse;

import java.util.List;

@RestController
public class MonitorApi extends AbstractMVC {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("/monitor/monitor-data-get")
    public RestfulResponse monitorDataGet(MDataPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        if (request.getNodeType() == null) {
            response.setSuccess(false);
            response.setMsg("nodeType can not be null.");
            return response;
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            response.setSuccess(false);
            response.setMsg("Search time range must be input.");
            return response;
        }
        if (StringUtils.isNotEmpty(request.getIdentity())) {
            request.setNodeGroup(null);
        }

        List<? extends MDataPo> rows = null;
        switch (request.getNodeType()) {
            case TASK_CLIENT:
                rows = appContext.getBackendJobClientMAccess().querySum(request);
                break;
            case TASK_DISPATCH:
                rows = appContext.getBackendJobTrackerMAccess().querySum(request);
                break;
            case TASK_EXECUTER:
                rows = appContext.getBackendTaskTrackerMAccess().querySum(request);
                break;
        }
        response.setSuccess(true);
        response.setRows(rows);
        response.setResults(CollectionUtils.sizeOf(rows));
        return response;
    }

    @RequestMapping("/monitor/jvm-monitor-data-get")
    public RestfulResponse jvmMDataGet(MDataPaginationReq request) {
        RestfulResponse response = new RestfulResponse();
        if (request.getJvmType() == null) {
            response.setSuccess(false);
            response.setMsg("jvmType can not be null.");
            return response;
        }
        if (request.getStartTime() == null || request.getEndTime() == null) {
            response.setSuccess(false);
            response.setMsg("Search time range must be input.");
            return response;
        }
        if (StringUtils.isNotEmpty(request.getIdentity())) {
            request.setNodeGroup(null);
        }

        List<? extends MDataPo> rows = null;
        switch (request.getJvmType()) {
            case GC:
                rows = appContext.getBackendJVMGCAccess().queryAvg(request);
                break;
            case MEMORY:
                rows = appContext.getBackendJVMMemoryAccess().queryAvg(request);
                break;
            case THREAD:
                rows = appContext.getBackendJVMThreadAccess().queryAvg(request);
                break;
        }
        response.setSuccess(true);
        response.setRows(rows);
        response.setResults(CollectionUtils.sizeOf(rows));
        return response;
    }

}
