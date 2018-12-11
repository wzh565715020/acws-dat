package com.tyyd.framework.dat.core.domain;


import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.exception.JobSubmitException;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.support.CronExpression;
import com.tyyd.framework.dat.remoting.annotation.NotNull;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Task implements Serializable {

    private static final long serialVersionUID = 7881199011994149340L;

    @NotNull
    private String taskId;
    /**
     * 优先级 (数值越大 优先级越低)
     */
    private Integer priority = 100;
    // 提交的节点 （可以手动指定）
    private String submitNodeGroup;
    // 执行的节点
    @NotNull
    private String taskTrackerNodeGroup;

    private Map<String, String> extParams;
    // 是否要反馈给客户端
    private boolean needFeedback = false;
    // 已经重试的次数 (用户不要设置)
    private int retryTimes = 0;
    // 该任务最大的重试次数
    private int maxRetryTimes = 0;
    /**
     * 执行表达式 和 quartz 的一样
     * 如果这个为空，表示立即执行的
     */
    private String cronExpression;

    /**
     * 重复次数 (-1 表示无限制重复)
     */
    private int repeatCount = 0;
    /**
     * 已经重复的次数, (用户不要设置)
     */
    private Integer repeatedCount = 0;
    /**
     * 重复interval
     */
    private Long repeatInterval;
    /**
     * 任务的最触发发时间
     * 如果设置了 cronExpression， 那么这个字段没用
     */
    private Long triggerTime;
    /**
     * 当任务队列中存在这个任务的时候，是否替换更新
     */
    private boolean replaceOnExist = false;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSubmitNodeGroup() {
        return submitNodeGroup;
    }

    public void setSubmitNodeGroup(String submitNodeGroup) {
        this.submitNodeGroup = submitNodeGroup;
    }

    public String getTaskTrackerNodeGroup() {
        return taskTrackerNodeGroup;
    }

    public void setTaskTrackerNodeGroup(String taskTrackerNodeGroup) {
        this.taskTrackerNodeGroup = taskTrackerNodeGroup;
    }

    public boolean isNeedFeedback() {
        return needFeedback;
    }

    public Integer getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public void setNeedFeedback(boolean needFeedback) {
        this.needFeedback = needFeedback;
    }

    public Map<String, String> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, String> extParams) {
        this.extParams = extParams;
    }

    public String getParam(String key) {
        if (extParams == null) {
            return null;
        }
        return extParams.get(key);
    }

    public void setParam(String key, String value) {
        if (extParams == null) {
            extParams = new HashMap<String, String>();
        }
        extParams.put(key, value);
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public boolean isCron() {
        return this.cronExpression != null && !"".equals(this.cronExpression.trim());
    }

    public boolean isRepeatable() {
        return (this.repeatInterval != null && this.repeatInterval > 0) && (this.repeatCount >= -1 && this.repeatCount != 0);
    }

    public void setTriggerDate(Date date) {
        if (date != null) {
            this.triggerTime = date.getTime();
        }
    }

    public Long getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Long triggerTime) {
        this.triggerTime = triggerTime;
    }

    public boolean isReplaceOnExist() {
        return replaceOnExist;
    }

    public void setReplaceOnExist(boolean replaceOnExist) {
        this.replaceOnExist = replaceOnExist;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public int getRepeatCount() {
        return repeatCount;
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
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

    public void checkField() throws JobSubmitException {
        if (taskId == null) {
            throw new JobSubmitException("taskId can not be null! job is " + toString());
        }
        if (taskTrackerNodeGroup == null) {
            throw new JobSubmitException("taskTrackerNodeGroup can not be null! job is " + toString());
        }
        if (StringUtils.isNotEmpty(cronExpression) && !CronExpression.isValidExpression(cronExpression)) {
            throw new JobSubmitException("cronExpression invalid! job is " + toString());
        }
        if (maxRetryTimes < 0) {
            throw new JobSubmitException("maxRetryTimes invalid, must be great than zero! job is " + toString());
        }
        if (repeatCount < -1) {
            throw new JobSubmitException("repeatCount invalid, must be great than -1! job is " + toString());
        }
    }
}
