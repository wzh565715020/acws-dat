package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.remoting.exception.RemotingCommandFieldCheckException;

import java.util.List;

public class TaskAskRequest extends AbstractRemotingCommandBody {

	private static final long serialVersionUID = 1993281575847386175L;
	
	List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public void checkFields() throws RemotingCommandFieldCheckException {
        if (ids == null || ids.size() == 0) {
            throw new RemotingCommandFieldCheckException("Ids could not be empty");
        }
    }
}
