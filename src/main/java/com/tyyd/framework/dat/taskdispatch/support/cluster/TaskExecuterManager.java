package com.tyyd.framework.dat.taskdispatch.support.cluster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tyyd.framework.dat.admin.request.TaskQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.loadbalance.LoadBalance;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskExecuterManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskExecuterManager.class);
	// 单例
	private TaskDispatcherAppContext appContext;

	public TaskExecuterManager(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}
	 public Node getTaskExecuterNode() {
		 LOGGER.info("获取任务执行节点开始");
		 List<Node> taskExecuters = appContext.getSubscribedNodeManager().getNodeList(NodeType.TASK_EXECUTER);
		 if (taskExecuters == null || taskExecuters.isEmpty()) {
			LOGGER.info("没有任务执行节点");
			return null;
		}
		TaskQueueReq request = new TaskQueueReq();
		request.setLimit(Integer.MAX_VALUE);
		PaginationRsp<TaskPo> paginationRsp = appContext.getExecutingTaskQueue().pageSelect(request);
		if (paginationRsp == null || paginationRsp.getRows()== null || paginationRsp.getRows().isEmpty()) {
			LoadBalance loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig());
			return loadBalance.select(taskExecuters, "");			
		}
		List<TaskPo> executingTasks = paginationRsp.getRows();
		Map<String, AtomicInteger> taskExcuterMap = new HashMap<String, AtomicInteger>();
		for (TaskPo taskPo : executingTasks) {
			if (taskExcuterMap.containsKey(taskPo.getTaskExecuteNode())) {
				taskExcuterMap.get(taskPo.getTaskExecuteNode()).getAndIncrement();
			}else {
				taskExcuterMap.put(taskPo.getTaskExecuteNode(),new AtomicInteger(1));
			}
		}
		 List<Node> newTaskExecuters = new ArrayList<Node>();
		for(Node node : taskExecuters) {
			if (taskExcuterMap.containsKey(node.getIdentity())) {
				node.setAvailableThreads(node.getThreads() - taskExcuterMap.get(node.getIdentity()).intValue());
			}
			if (node.getAvailableThreads()>0) {
				newTaskExecuters.add(node);
			}
		}
		LOGGER.info("" + newTaskExecuters);
		if (newTaskExecuters.isEmpty()) {
			return null;
		}
		LoadBalance loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig());
		return loadBalance.select(newTaskExecuters, "");
	 }
}


