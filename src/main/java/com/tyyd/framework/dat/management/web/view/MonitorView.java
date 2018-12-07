package com.tyyd.framework.dat.management.web.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.DateUtils;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.management.cluster.BackendAppContext;
import com.tyyd.framework.dat.management.web.vo.NodeInfo;
import com.tyyd.framework.dat.queue.domain.NodeGroupPo;

import java.util.*;

/**
 * @author Robert HG (254963746@qq.com) on 8/22/15.
 */
@Controller
public class MonitorView {

    @Autowired
    private BackendAppContext appContext;

    @RequestMapping("monitor/jobtracker-monitor")
    public String jobTrackerMonitor(Model model) {

        initTimeRange(model);

        List<String> taskTrackers = appContext.getBackendJobTrackerMAccess().getJobTrackers();
        model.addAttribute("jobTrackers", taskTrackers);

        return "monitor/jobtrackerMonitor";
    }

    @RequestMapping("monitor/tasktracker-monitor")
    public String taskTrackerMonitor(Model model) {

        initTimeRange(model);

        List<NodeGroupPo> nodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.TASK_EXECUTER);
        List<NodeInfo> nodeInfos = appContext.getBackendTaskTrackerMAccess().getTaskTrackers();
        setGroupIdMap(model, nodeGroups, nodeInfos);

        return "monitor/tasktrackerMonitor";
    }

    @RequestMapping("monitor/jobClient-monitor")
    public String jobClientMonitor(Model model) {

        initTimeRange(model);
        List<NodeGroupPo> nodeGroups = appContext.getNodeGroupStore().getNodeGroup(NodeType.TASK_CLIENT);
        List<NodeInfo> nodeInfos = appContext.getBackendJobClientMAccess().getJobClients();
        setGroupIdMap(model, nodeGroups, nodeInfos);

        return "monitor/jobClientMonitor";
    }

    private void initTimeRange(Model model) {
        Date endDate = new Date();
        model.addAttribute("startTime", DateUtils.formatYMD_HMS(DateUtils.addHour(endDate, -3)));
        model.addAttribute("endTime", DateUtils.formatYMD_HMS(endDate));
    }

    private void setGroupIdMap(Model model, List<NodeGroupPo> nodeGroups, List<NodeInfo> nodeInfos) {
        Map<String, Set<String>> groupIdMap = new HashMap<String, Set<String>>();
        if (CollectionUtils.isNotEmpty(nodeGroups)) {

            for (NodeGroupPo nodeGroup : nodeGroups) {
                groupIdMap.put(nodeGroup.getName(), new HashSet<String>());
            }

            if (CollectionUtils.isNotEmpty(nodeInfos)) {
                for (NodeInfo nodeInfo : nodeInfos) {
                    Set<String> identities = groupIdMap.get(nodeInfo.getNodeGroup());
                    if (identities == null) {
                        identities = new HashSet<String>();
                        groupIdMap.put(nodeInfo.getNodeGroup(), identities);
                    }
                    identities.add(nodeInfo.getIdentity());
                }
            }
        }
        model.addAttribute("groupIdMap", JSON.toJSONString(groupIdMap));
    }

}
