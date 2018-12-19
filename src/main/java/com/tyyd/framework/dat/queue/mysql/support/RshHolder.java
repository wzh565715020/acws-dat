package com.tyyd.framework.dat.queue.mysql.support;

import com.tyyd.framework.dat.biz.logger.domain.TaskLogPo;
import com.tyyd.framework.dat.biz.logger.domain.LogType;
import com.tyyd.framework.dat.core.constant.Level;
import com.tyyd.framework.dat.queue.domain.PoolPo;
import com.tyyd.framework.dat.queue.domain.TaskPo;
import com.tyyd.framework.dat.store.jdbc.dbutils.ResultSetHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
        TaskPo taskPo = new TaskPo();
        taskPo.setRetryTimes(rs.getInt("retry_times"));
        taskPo.setMaxRetryTimes(rs.getInt("max_retry_times"));
        taskPo.setTaskId(rs.getString("task_id"));
        taskPo.setTaskName(rs.getString("task_name"));
        taskPo.setTaskType(rs.getString("task_type"));
        taskPo.setTaskClass(rs.getString("task_class"));
        taskPo.setTaskExecType(rs.getString("task_exec_type"));
        taskPo.setCreateDate(rs.getLong("create_date"));
        taskPo.setUpdateDate(rs.getLong("update_date"));
        taskPo.setSubmitNode(rs.getString("submit_node"));
        taskPo.setTaskExecuteNode(rs.getString("task_execute_node"));
        taskPo.setParams(rs.getString("params"));
        taskPo.setCron(rs.getString("cron"));
        taskPo.setTriggerTime(rs.getLong("trigger_time"));
        taskPo.setRepeatCount(rs.getInt("repeat_count"));
        taskPo.setRepeatedCount(rs.getInt("repeated_count"));
        taskPo.setRepeatInterval(rs.getLong("repeat_interval"));
        taskPo.setId(rs.getString("id"));
        return taskPo;
    }

    public static final ResultSetHandler<List<TaskLogPo>> JOB_LOGGER_LIST_RSH = new ResultSetHandler<List<TaskLogPo>>() {
        @Override
        public List<TaskLogPo> handle(ResultSet rs) throws SQLException {
            List<TaskLogPo> result = new ArrayList<TaskLogPo>();
            while (rs.next()) {
                TaskLogPo jobLogPo = new TaskLogPo();
                jobLogPo.setLogTime(rs.getLong("log_time"));
                jobLogPo.setCreateTime(rs.getLong("createTime"));
                jobLogPo.setLogType(LogType.valueOf(rs.getString("log_type")));
                jobLogPo.setSuccess(rs.getBoolean("success"));
                jobLogPo.setMsg(rs.getString("msg"));
                jobLogPo.setTaskExecuteNode(rs.getString("task_execute_node"));
                jobLogPo.setLevel(Level.valueOf(rs.getString("level")));
                jobLogPo.setTaskId(rs.getString("task_id"));
                jobLogPo.setId(rs.getString("task_run_id"));
                jobLogPo.setSubmitNode(rs.getString("submit_node"));
                jobLogPo.setCron(rs.getString("cron"));
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


