package com.tyyd.framework.dat.queue.mysql.support;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.json.TypeReference;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RshHolder {

    public static final ResultSetHandler<TaskPo> TASK_PO_RSH = new ResultSetHandler<TaskPo>() {
        @Override
        public TaskPo handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return null;
            }
            return getTaskPo(rs);
        }
    };

    public static final ResultSetHandler<List<TaskPo>> TASK_PO_LIST_RSH = new ResultSetHandler<List<TaskPo>>() {
        @Override
        public List<TaskPo> handle(ResultSet rs) throws SQLException {
            List<TaskPo> taskPos = new ArrayList<TaskPo>();
            while (rs.next()) {
                taskPos.add(getTaskPo(rs));
            }
            return taskPos;
        }
    };
    public static final ResultSetHandler<PoolPo> POOL_PO_RSH = new ResultSetHandler<PoolPo>() {
        @Override
        public PoolPo handle(ResultSet rs) throws SQLException {
            if (!rs.next()) {
                return null;
            }
            return getPoolPo(rs);
        }
		
    };
    public static final ResultSetHandler<List<PoolPo>> POOL_PO_LIST_RSH = new ResultSetHandler<List<PoolPo>>() {
        @Override
        public List<PoolPo> handle(ResultSet rs) throws SQLException {
            List<PoolPo> poolPos = new ArrayList<PoolPo>();
            while (rs.next()) {
                poolPos.add(getPoolPo(rs));
            }
            return poolPos;
        }
    };
    private static PoolPo getPoolPo(ResultSet rs) throws SQLException {
    	PoolPo poolPo = new PoolPo();
    	poolPo.setPoolId(rs.getString("pool_id"));
    	poolPo.setPoolName(rs.getString("pool_name"));
    	poolPo.setMaxCount(rs.getInt("max_count"));
    	poolPo.setAvailableCount(rs.getInt("available_count"));
    	poolPo.setTaskIds(rs.getString("task_ids"));
    	poolPo.setMemo(rs.getString("memo"));
    	poolPo.setCreateDate(rs.getLong("create_date"));
    	poolPo.setUpdateDate(rs.getLong("update_date"));
    	poolPo.setCreateUserId(rs.getString("create_userid"));
    	poolPo.setUpdateUserId(rs.getString("update_userid"));
		return poolPo;
	}
    private static TaskPo getTaskPo(ResultSet rs) throws SQLException {
        TaskPo jobPo = new TaskPo();
        jobPo.setRetryTimes(rs.getInt("retry_times"));
        jobPo.setMaxRetryTimes(rs.getInt("max_retry_times"));
        jobPo.setTaskId(rs.getString("task_id"));
        jobPo.setTaskName(rs.getString("task_name"));
        jobPo.setTaskType(rs.getString("task_type"));
        jobPo.setTaskClass(rs.getString("task_class"));
        jobPo.setTaskExecType(rs.getString("task_exec_type"));
        jobPo.setCreateDate(rs.getLong("create_date"));
        jobPo.setUpdateDate(rs.getLong("update_date"));
        jobPo.setSubmitNode(rs.getString("submit_node"));
        jobPo.setTaskExecuteNode(rs.getString("task_execute_node"));
        jobPo.setParams(rs.getString("params"));
        jobPo.setCron(rs.getString("cron"));
        jobPo.setTriggerTime(rs.getLong("trigger_time"));
        jobPo.setRepeatCount(rs.getInt("repeat_count"));
        jobPo.setRepeatedCount(rs.getInt("repeated_count"));
        jobPo.setRepeatInterval(rs.getLong("repeat_interval"));
        jobPo.setId(rs.getString("id"));
        return jobPo;
    }

    public static final ResultSetHandler<List<TaskLogPo>> JOB_LOGGER_LIST_RSH = new ResultSetHandler<List<TaskLogPo>>() {
        @Override
        public List<TaskLogPo> handle(ResultSet rs) throws SQLException {
            List<TaskLogPo> result = new ArrayList<TaskLogPo>();
            while (rs.next()) {
                TaskLogPo jobLogPo = new TaskLogPo();
                jobLogPo.setLogTime(rs.getLong("log_time"));
                jobLogPo.setGmtCreated(rs.getLong("gmt_created"));
                jobLogPo.setLogType(LogType.valueOf(rs.getString("log_type")));
                jobLogPo.setSuccess(rs.getBoolean("success"));
                jobLogPo.setMsg(rs.getString("msg"));
                jobLogPo.setTaskTrackerIdentity(rs.getString("task_tracker_identity"));
                jobLogPo.setLevel(Level.valueOf(rs.getString("level")));
                jobLogPo.setTaskId(rs.getString("task_id"));
                jobLogPo.setJobId(rs.getString("job_id"));
                jobLogPo.setPriority(rs.getInt("priority"));
                jobLogPo.setSubmitNodeGroup(rs.getString("submit_node_group"));
                jobLogPo.setTaskTrackerNodeGroup(rs.getString("task_tracker_node_group"));
                jobLogPo.setExtParams(JSON.parse(rs.getString("ext_params"), new TypeReference<Map<String, String>>() {
                }));
                jobLogPo.setInternalExtParams(JSON.parse(rs.getString("internal_ext_params"), new TypeReference<HashMap<String, String>>(){}));
                jobLogPo.setNeedFeedback(rs.getBoolean("need_feedback"));
                jobLogPo.setCronExpression(rs.getString("cron_expression"));
                jobLogPo.setTriggerTime(rs.getLong("trigger_time"));
                jobLogPo.setRetryTimes(rs.getInt("retry_times"));
                jobLogPo.setMaxRetryTimes(rs.getInt("max_retry_times"));
                jobLogPo.setRepeatCount(rs.getInt("repeat_count"));
                jobLogPo.setRepeatedCount(rs.getInt("repeated_count"));
                jobLogPo.setRepeatInterval(rs.getLong("repeat_interval"));
                result.add(jobLogPo);
            }
            return result;
        }
    };
}


