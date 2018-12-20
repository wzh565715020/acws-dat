package com.tyyd.framework.dat.queue.domain;

import com.tyyd.framework.dat.core.domain.TaskExecType;
import com.tyyd.framework.dat.core.domain.TaskType;
import com.tyyd.framework.dat.core.json.JSON;

/**
 * 存储的task对象
 */
public class TaskPo {
	private String id;
	private String taskId;
	private String taskName;
	private String taskClass;
	private String taskType;
	private String taskExecType;
	private String poolId;
	/**
	 * 执行时间表达式 (和 quartz 表达式一样)
	 */
	private String cron;

	private String params;

	private Integer status;
	private Long triggerTime;
	/**
	 * 重复次数
	 */
	private Integer repeatCount = 0;
	
	private Integer isRunning;

	private String memo;
	/**
	 * 提交客户端的节点组
	 */
	private String submitNode;
	private String taskExecuteNode;
	/**
	 * 已经重复的次数
	 */
	private Integer repeatedCount = 0;
	private Long repeatInterval;
	// 创建时间
	private Long createDate;
	// 修改时间
	private Long updateDate;
	private String createUserid;
	private String updateUserid;
	private Integer retryTimes = 0;
	private Integer maxRetryTimes = 0;

	public Integer getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(Integer retryTimes) {
		this.retryTimes = retryTimes;
	}

	public Integer getMaxRetryTimes() {
		return maxRetryTimes;
	}

	public void setMaxRetryTimes(Integer maxRetryTimes) {
		this.maxRetryTimes = maxRetryTimes;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskClass() {
		return taskClass;
	}

	public void setTaskClass(String taskClass) {
		this.taskClass = taskClass;
	}

	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}

	public String getTaskExecType() {
		return taskExecType;
	}

	public void setTaskExecType(String taskExecType) {
		this.taskExecType = taskExecType;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getRepeatCount() {
		return repeatCount;
	}

	public void setRepeatCount(Integer repeatCount) {
		this.repeatCount = repeatCount;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getSubmitNode() {
		return submitNode;
	}

	public void setSubmitNode(String submitNode) {
		this.submitNode = submitNode;
	}

	public String getTaskExecuteNode() {
		return taskExecuteNode;
	}

	public void setTaskExecuteNode(String taskExecuteNode) {
		this.taskExecuteNode = taskExecuteNode;
	}

	public Integer getRepeatedCount() {
		return repeatedCount;
	}

	public void setRepeatedCount(Integer repeatedCount) {
		this.repeatedCount = repeatedCount;
	}

	public Long getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Long createDate) {
		this.createDate = createDate;
	}

	public Long getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Long updateDate) {
		this.updateDate = updateDate;
	}

	public String getCreateUserid() {
		return createUserid;
	}

	public void setCreateUserid(String createUserid) {
		this.createUserid = createUserid;
	}

	public String getUpdateUserid() {
		return updateUserid;
	}

	public void setUpdateUserid(String updateUserid) {
		this.updateUserid = updateUserid;
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Long triggerTime) {
		this.triggerTime = triggerTime;
	}

	public boolean isRepeatable() {
		return TaskType.LOOP.getCode().equals(taskType);
	}

	public boolean isCron() {
		return TaskType.SINGLE.getCode().equals(taskType) && TaskExecType.SCHEDULETIME.getCode().equals(taskExecType);
	}

	public Long getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(Long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public String getPoolId() {
		return poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}

	public Integer getIsRunning() {
		return isRunning;
	}

	public void setIsRunning(Integer isRunning) {
		this.isRunning = isRunning;
	}

}
