package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.core.domain.BizLog;

import java.util.List;

public class BizLogSendRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 6477068771579478184L;
	private List<BizLog> bizLogs;

    public List<BizLog> getBizLogs() {
        return bizLogs;
    }

    public void setBizLogs(List<BizLog> bizLogs) {
        this.bizLogs = bizLogs;
    }
}
