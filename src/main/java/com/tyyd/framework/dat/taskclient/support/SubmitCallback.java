package com.tyyd.framework.dat.taskclient.support;

import com.tyyd.framework.dat.remoting.protocol.RemotingCommand;

public interface SubmitCallback {

    public void call(final RemotingCommand responseCommand);

}
