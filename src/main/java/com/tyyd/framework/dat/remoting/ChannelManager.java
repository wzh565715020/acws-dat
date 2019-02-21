package com.tyyd.framework.dat.remoting;

import java.util.List;


import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.remoting.ChannelWrapper;

/**
 * 管理channel
 */
public interface ChannelManager {


	public void start();

	public void stop();

	public List<ChannelWrapper> getChannels();

	/**
	 * 根据 节点唯一编号得到 channel
	 */
	public ChannelWrapper getChannel(NodeType nodeType, String identity);

	/**
	 * 添加channel
	 */
	public void offerChannel(ChannelWrapper channel);

	public Long getOfflineTimestamp(String identity);

	public void removeChannel(ChannelWrapper channel);
}
