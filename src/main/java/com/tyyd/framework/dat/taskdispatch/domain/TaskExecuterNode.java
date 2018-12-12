package com.tyyd.framework.dat.taskdispatch.domain;



import java.util.concurrent.atomic.AtomicInteger;

import com.tyyd.framework.dat.taskdispatch.channel.ChannelWrapper;

/**
 * TaskExecuterNode状态对象
 */
public class TaskExecuterNode{
    // 可用线程数
    public AtomicInteger availableThread;
    // 唯一标识
    public String identity;
    // 该节点的channel
    public ChannelWrapper channel;

    public Long timestamp = null;
    
    private String ip;
    
    private Integer port = 0;
    
    public TaskExecuterNode(int availableThread, String identity, ChannelWrapper channel) {
        this.availableThread = new AtomicInteger(availableThread);
        this.identity = identity;
        this.channel = channel;
    }

    public TaskExecuterNode(String identity) {
        this.identity = identity;
    }

    public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

    public AtomicInteger getAvailableThread() {
        return availableThread;
    }
    public Integer getAvailableThreadInteger() {
        return availableThread.get();
    }
    public void setAvailableThread(int availableThread) {
        this.availableThread = new AtomicInteger(availableThread);
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public ChannelWrapper getChannel() {
        return channel;
    }

    public void setChannel(ChannelWrapper channel) {
        this.channel = channel;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskExecuterNode)) return false;

        TaskExecuterNode that = (TaskExecuterNode) o;

        if (identity != null ? !identity.equals(that.identity) : that.identity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identity != null ? identity.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TaskTrackerNode{" +
                ", availableThread=" + (availableThread == null ? 0 : availableThread.get()) +
                ", identity='" + identity + '\'' +
                ", channel=" + channel +
                '}';
    }
}