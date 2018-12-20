package com.tyyd.framework.dat.management.web.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tyyd.framework.dat.core.commons.utils.DateUtils;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

import java.util.Date;

@Controller
public class CommonView {

    private TaskDispatcherAppContext appContext;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("node-manager")
    public String nodeManagerUI() {
        return "nodeManager";
    }

    @RequestMapping("node-group-manager")
    public String nodeGroupManagerUI() {
        return "nodeGroupManager";
    }

    @RequestMapping("node-onoffline-log")
    public String nodeOnOfflineLogUI(Model model) {
        model.addAttribute("startLogTime", DateUtils.formatYMD_HMS(DateUtils.addDay(new Date(), -10)));
        model.addAttribute("endLogTime", DateUtils.formatYMD_HMS(new Date()));
        return "nodeOnOfflineLog";
    }

    @RequestMapping("node-jvm-info")
    public String nodeJVMInfo(Model model, String identity) {
        model.addAttribute("identity", identity);
        return "nodeJvmInfo";
    }

    @RequestMapping("task-add")
    public String addJobUI(Model model) {
        setAttr(model);
        return "jobAdd";
    }

    @RequestMapping("task-logger")
    public String jobLoggerUI(Model model, String taskId, String taskTrackerNodeGroup,
                              Date startLogTime, Date endLogTime) {
        model.addAttribute("taskId", taskId);
        model.addAttribute("taskTrackerNodeGroup", taskTrackerNodeGroup);
        if (startLogTime == null) {
            startLogTime = DateUtils.addMinute(new Date(), -10);
        }
        model.addAttribute("startLogTime", DateUtils.formatYMD_HMS(startLogTime));
        if (endLogTime == null) {
            endLogTime = new Date();
        }
        model.addAttribute("endLogTime", DateUtils.formatYMD_HMS(endLogTime));
        setAttr(model);
        return "jobLogger";
    }

    @RequestMapping("cron-task-queue")
    public String cronJobQueueUI(Model model) {
        setAttr(model);
        return "cronJobQueue";
    }

    @RequestMapping("repeat-task-queue")
    public String repeatJobQueueUI(Model model) {
        setAttr(model);
        return "repeatJobQueue";
    }

    @RequestMapping("executable-task-queue")
    public String executableJobQueueUI(Model model) {
        setAttr(model);
        return "executableJobQueue";
    }

    @RequestMapping("executing-task-queue")
    public String executingJobQueueUI(Model model) {
        setAttr(model);
        return "executingJobQueue";
    }

    @RequestMapping("load-task")
    public String loadJobUI(Model model) {
        setAttr(model);
        return "loadJob";
    }

    @RequestMapping("cron_generator_iframe")
    public String cronGeneratorIframe(Model model){
        return "cron/cronGenerator";
    }

	@RequestMapping("suspend-task-queue")
	public String suspendJobQueueUI(Model model) {
		setAttr(model);
		return "suspendJobQueue";
	}

    private void setAttr(Model model) {
    }

}
