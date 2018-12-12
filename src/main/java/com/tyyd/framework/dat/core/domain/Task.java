package com.tyyd.framework.dat.core.domain;


import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.exception.JobSubmitException;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.support.CronExpression;
import com.tyyd.framework.dat.remoting.annotation.NotNull;

import java.io.Serializable;

public class Task implements Serializable {

    private static final long serialVersionUID = 7881199011994149340L;
	@NotNull
    private String taskId;
	private String taskName;
	private String taskClass;
	private String taskType;
	private String taskExecType;
	/**
	 * 执行时间表达式 (和 quartz 表达式一样)
	 */
	private String cron;

	private String params;

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
	/**
	 * 已经重复的次数
	 */
	private Integer repeatedCount = 0;
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


	private Long repeatInterval;

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


	public Integer getRepeatedCount() {
		return repeatedCount;
	}

	public void setRepeatedCount(Integer repeatedCount) {
		this.repeatedCount = repeatedCount;
	}


	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}

	public Long getTriggerTime() {
		return triggerTime;
	}

	public void setTriggerTime(Long triggerTime) {
		this.triggerTime = triggerTime;
	}

	public Long getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(Long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}


    public void checkField() throws JobSubmitException {
        if (taskId == null) {
            throw new JobSubmitException("taskId can not be null! job is " + toString());
        }
        if (StringUtils.isNotEmpty(cron) && !CronExpression.isValidExpression(cron)) {
            throw new JobSubmitException("cronExpression invalid! job is " + toString());
        }
        if (repeatCount < -1) {
            throw new JobSubmitException("repeatCount invalid, must be great than -1! job is " + toString());
        }
    }

	public boolean isCron() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isRepeatable() {
		// TODO Auto-generated method stub
		return false;
	}
}
