package com.tyyd.framework.dat.taskdispatch.domain;

import com.tyyd.framework.dat.taskdispatch.channel.ChannelWrapper;

/**
 * 客户端节点
 */
public class TaskClientNode {

    // 节点组名称
    public String nodeGroup;
    // 唯一标识
    public String identity;
    // 该节点的channel
    public ChannelWrapper channel;

    public TaskClientNode(String nodeGroup, String identity, ChannelWrapper channel) {
        this.nodeGroup = nodeGroup;
        this.identity = identity;
        this.channel = channel;
    }

    public TaskClientNode(String identity) {
        this.identity = identity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskClientNode)) return false;

        TaskClientNode that = (TaskClientNode) o;

        if (identity != null ? !identity.equals(that.identity) : that.identity != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return identity != null ? identity.hashCode() : 0;
    }

    public String getNodeGroup() {
        return nodeGroup;
    }

    public void setNodeGroup(String nodeGroup) {
        this.nodeGroup = nodeGroup;
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

    @Override
    public String toString() {
        return "JobClientNode{" +
                "nodeGroup='" + nodeGroup + '\'' +
                ", identity='" + identity + '\'' +
                ", channel=" + channel +
                '}';
    }
}
