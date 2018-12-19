package com.tyyd.framework.dat.biz.logger.domain;

import com.tyyd.framework.dat.core.constant.Level;

import java.util.Map;

/**
 *         任务执行 日志
 */
public class TaskLogPo {

    // 日志记录时间
    private Long logTime;
    // 日志记录时间
    private Long createTime;
    // 日志类型
    private LogType logType;
    private boolean success;
    private String msg;

    // 日志记录级别
    private Level level;

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
	public Long getLogTime() {
		return logTime;
	}
	public void setLogTime(Long logTime) {
		this.logTime = logTime;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	public LogType getLogType() {
		return logType;
	}
	public void setLogType(LogType logType) {
		this.logType = logType;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public Level getLevel() {
		return level;
	}
	public void setLevel(Level level) {
		this.level = level;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getPoolId() {
		return poolId;
	}
	public void setPoolId(String poolId) {
		this.poolId = poolId;
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
	public Long getTriggerTime() {
		return triggerTime;
	}
	public void setTriggerTime(Long triggerTime) {
		this.triggerTime = triggerTime;
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
	public Long getRepeatInterval() {
		return repeatInterval;
	}
	public void setRepeatInterval(Long repeatInterval) {
		this.repeatInterval = repeatInterval;
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
	
}
