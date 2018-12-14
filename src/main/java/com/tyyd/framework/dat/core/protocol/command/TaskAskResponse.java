package com.tyyd.framework.dat.core.protocol.command;

import java.util.List;

public class TaskAskResponse extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 6614340681500484560L;
	/**
     * 返回不在执行中的id
     */
    List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
