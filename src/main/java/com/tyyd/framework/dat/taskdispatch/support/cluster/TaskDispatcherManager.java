package com.tyyd.framework.dat.taskdispatch.support.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.omg.CORBA.PolicyListHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.sleepycat.bind.tuple.BooleanBinding;
import com.tyyd.framework.dat.admin.request.PoolQueueReq;
import com.tyyd.framework.dat.admin.response.PaginationRsp;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.commons.concurrent.ConcurrentHashSet;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.taskdispatch.domain.TaskDispatcherAppContext;

public class TaskDispatcherManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(TaskDispatcherManager.class);
	// 单例
	private final ConcurrentHashSet<Node> taskDispatcherSet = new ConcurrentHashSet<Node>();
	private TaskDispatcherAppContext appContext;

	private final ReentrantLock globalLock = new ReentrantLock();

	public TaskDispatcherManager(TaskDispatcherAppContext appContext) {
		this.appContext = appContext;
	}

	/**
	 * 添加节点
	 */
	public void addNode(Node node) {

		if (taskDispatcherSet.contains(node)) {
			return;
		}
		taskDispatcherSet.add(node);
		if (appContext.getMasterNode().getIdentity().equals(appContext.getConfig().getIdentity())) {
			addNodeRedistribution(node);
		}
	}

	/**
	 * 删除节点
	 *
	 * @param node
	 */
	public void removeNode(Node node) {
		taskDispatcherSet.remove(node);
		if (appContext.getMasterNode().getIdentity().equals(appContext.getConfig().getIdentity())) {
			removeNodeRedistribution(node);
		}
	}

	public void addNodeRedistribution(Node node) {
		globalLock.lock();
		try {
			PoolQueueReq request = new PoolQueueReq();
			request.setLimit(Integer.MAX_VALUE);
			PaginationRsp<PoolPo> paginationRsp = appContext.getPoolQueue().pageSelect(request);
			List<PoolPo> poolPos = paginationRsp.getRows();
			if (poolPos == null || poolPos.isEmpty()) {
				return;
			}
			int poolSize = poolPos.size();
			int dispatcherSize = taskDispatcherSet.size();
			if (poolSize < dispatcherSize) {
				return;
			}
			int average = 0;
			if (dispatcherSize > 0) {
				average = poolSize/dispatcherSize;
			}
			
			List<PoolPo> pools = appContext.getPoolQueue().getPoolGreaterAverage(average);
			Map<String, List<PoolPo>> nodeMap = new HashMap<String, List<PoolPo>>();
			boolean hasWfp = false;
			String defaultId = "default";
			for (PoolPo poolPo : pools) {
				String nodeId = poolPo.getNodeId();
				if (nodeId == null || "".equals(nodeId)) {
					nodeId = defaultId;
					hasWfp = true;
				}
				if (nodeMap.containsKey(nodeId)) {
					nodeMap.get(nodeId).add(poolPo);
				}else {
					nodeMap.put(nodeId, new ArrayList<PoolPo>());
					nodeMap.get(nodeId).add(poolPo);
				}
			}
			if (hasWfp) {
				for(PoolPo poolPo : nodeMap.get(defaultId)) {
					PoolQueueReq  updateReq = new PoolQueueReq();
					updateReq.setPoolId(poolPo.getPoolId());
					updateReq.setNodeId(node.getIdentity());
					appContext.getPoolQueue().updateByPoolId(updateReq);
				}
				return;
			}
			List<PoolNum> list = new ArrayList<PoolNum>();
			
			for (String id : nodeMap.keySet()) {
				PoolNum poolNum = new PoolNum();
				poolNum.setId(id);
				poolNum.setNum(nodeMap.get(id).size());
				list.add(poolNum);
			}
			
			Collections.sort(list);
			
			int fpNum = 0;
			if (average%list.size() == 0) {
				fpNum = average/list.size();
			}else {
				fpNum = average/list.size() + 1;
			}
			for (int i = 0 ; i < list.size(); i++) {
				for(int j = 0 ; j< fpNum ;j++) {
					PoolPo poolPo = nodeMap.get(list.get(i).getId()).get(j);
					PoolQueueReq  updateReq = new PoolQueueReq();
					updateReq.setPoolId(poolPo.getPoolId());
					updateReq.setNodeId(node.getIdentity());
					appContext.getPoolQueue().updateByPoolId(updateReq);
				}
			}
		} finally {
			globalLock.unlock();
		}

	}

	public void removeNodeRedistribution(Node node) {
		globalLock.lock();
		try {
			List<PoolPo> poolPos = appContext.getPoolQueue().getPoolByNodeId(node.getIdentity());
			if (poolPos == null || poolPos.isEmpty()) {
				return;
			}
			PoolQueueReq request = new PoolQueueReq();
			request.setNodeId(node.getIdentity());
			appContext.getPoolQueue().clearNodeByNodeId(request);
		} finally {
			globalLock.unlock();
		}
	}

	public void poolChangeRedistribution() {
		globalLock.lock();
		try {
			List<PoolPo> poolPos = appContext.getPoolQueue().getUndistributedPool();
			if (poolPos == null || poolPos.isEmpty()) {
				return;
			}
			PoolQueueReq request = new PoolQueueReq();
			request.setLimit(Integer.MAX_VALUE);
			PaginationRsp<PoolPo> paginationRsp = appContext.getPoolQueue().pageSelect(request);
			List<PoolPo> distributedPoolPos = paginationRsp.getRows();
			Map<String, List<PoolPo>> nodeMap = new HashMap<String, List<PoolPo>>();
			for (PoolPo poolPo : distributedPoolPos) {
				String nodeId = poolPo.getNodeId();
				if (nodeMap.containsKey(nodeId)) {
					nodeMap.get(nodeId).add(poolPo);
				}else {
					nodeMap.put(nodeId, new ArrayList<PoolPo>());
					nodeMap.get(nodeId).add(poolPo);
				}
			}
			List<PoolNum> list = new ArrayList<PoolNum>();
			
			for (String id : nodeMap.keySet()) {
				PoolNum poolNum = new PoolNum();
				poolNum.setId(id);
				poolNum.setNum(nodeMap.get(id).size());
				list.add(poolNum);
			}
			
			Collections.sort(list);
			
			int cicleNum = (poolPos.size()%list.size()==0)?poolPos.size()/list.size():poolPos.size()/list.size()+1;
			int undistributedNum = 0;
			for(int i =0 ; i < cicleNum ; i++) {
				for (int j = list.size()-1; j >= 0 && undistributedNum < poolPos.size() ; j--) {
					PoolQueueReq  updateReq = new PoolQueueReq();
					updateReq.setPoolId(poolPos.get(undistributedNum).getPoolId());
					updateReq.setNodeId(list.get(j).getId());
					appContext.getPoolQueue().updateByPoolId(updateReq);
				}
			}
		} finally {
			globalLock.unlock();
		}
	}
	public static class PoolNum implements Comparable<PoolNum> {
		private String id;
		private Integer num;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public int getNum() {
			return num;
		}
		public void setNum(int num) {
			this.num = num;
		}
		@Override
		public int compareTo(PoolNum o) {
			return -this.num.compareTo(o.num);
		}
		@Override
		public String toString() {
			return id + "--" + num;
			
		}
	}
}


