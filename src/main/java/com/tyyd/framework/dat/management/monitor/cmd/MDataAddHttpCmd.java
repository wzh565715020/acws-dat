package com.tyyd.framework.dat.management.monitor.cmd;


import java.util.List;

import com.tyyd.framework.dat.cmd.HttpCmdProc;
import com.tyyd.framework.dat.cmd.HttpCmdRequest;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.cmd.HttpCmdNames;
import com.tyyd.framework.dat.core.cmd.HttpCmdParamNames;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.domain.monitor.JobClientMData;
import com.tyyd.framework.dat.core.domain.monitor.JobTrackerMData;
import com.tyyd.framework.dat.core.domain.monitor.MData;
import com.tyyd.framework.dat.core.domain.monitor.MNode;
import com.tyyd.framework.dat.core.domain.monitor.TaskTrackerMData;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.json.TypeReference;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.management.monitor.MonitorAppContext;

/**
 * 监控数据添加CMD
 *
 */
public class MDataAddHttpCmd implements HttpCmdProc {

    private static final Logger LOGGER = LoggerFactory.getLogger(MDataAddHttpCmd.class);

    private MonitorAppContext appContext;

    public MDataAddHttpCmd(MonitorAppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public String nodeIdentity() {
        return appContext.getConfig().getIdentity();
    }

    @Override
    public String getCommand() {
        return HttpCmdNames.HTTP_CMD_ADD_M_DATA;
    }

    @Override
    public HttpCmdResponse execute(HttpCmdRequest request) throws Exception {

        String mNodeJson = request.getParam(HttpCmdParamNames.M_NODE);
        if (StringUtils.isEmpty(mNodeJson)) {
            return HttpCmdResponse.newResponse(false, "mData is empty");
        }
        MNode mNode = JSON.parse(mNodeJson, new TypeReference<MNode>() {
        }.getType());

        HttpCmdResponse response = paramCheck(mNode);
        if (response != null) {
            return response;
        }

        String mDataJson = request.getParam(HttpCmdParamNames.M_DATA);
        if (StringUtils.isEmpty(mDataJson)) {
            return HttpCmdResponse.newResponse(false, "mData is empty");
        }
        try {
            assert mNode != null;
            List<MData> mDatas = getMDataList(mNode.getNodeType(), mDataJson);
            appContext.getMDataSrv().addMDatas(mNode, mDatas);
        } catch (Exception e) {
            LOGGER.error("Add Monitor Data error: " + JSON.toJSONString(request), e);
            return HttpCmdResponse.newResponse(false, "Add Monitor Data error: " + e.getMessage());
        }

        LOGGER.info("Add Monitor Data success, mNode=" + mNodeJson + ", mData=" + mDataJson);

        return HttpCmdResponse.newResponse(true, "Add Monitor Data success");
    }


    private List<MData> getMDataList(NodeType nodeType, String mDataJson) {
        List<MData> mDatas = null;
        if (NodeType.TASK_EXECUTER == nodeType) {
            mDatas = JSON.parse(mDataJson, new TypeReference<List<TaskTrackerMData>>() {
            }.getType());
        } else if (NodeType.TASK_DISPATCH == nodeType) {
            mDatas = JSON.parse(mDataJson, new TypeReference<List<JobTrackerMData>>() {
            }.getType());
        } else if (NodeType.TASK_CLIENT == nodeType) {
            mDatas = JSON.parse(mDataJson, new TypeReference<List<JobClientMData>>() {
            }.getType());
        }
        return mDatas;
    }

    private HttpCmdResponse paramCheck(MNode mNode) {
        if (mNode == null) {
            return HttpCmdResponse.newResponse(false, "mNode is empty");
        }

        NodeType nodeType = mNode.getNodeType();
        if (nodeType == null || !(nodeType == NodeType.TASK_CLIENT || nodeType == NodeType.TASK_EXECUTER || nodeType == NodeType.TASK_DISPATCH)) {
            return HttpCmdResponse.newResponse(false, "nodeType error");
        }
        if (StringUtils.isEmpty(mNode.getIdentity())) {
            return HttpCmdResponse.newResponse(false, "identity is empty");
        }
        return null;
    }

}
