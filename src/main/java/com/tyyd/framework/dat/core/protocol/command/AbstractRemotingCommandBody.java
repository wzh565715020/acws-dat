package com.tyyd.framework.dat.core.protocol.command;

import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.remoting.RemotingCommandBody;
import com.tyyd.framework.dat.remoting.annotation.NotNull;
import com.tyyd.framework.dat.remoting.annotation.Nullable;
import com.tyyd.framework.dat.remoting.exception.RemotingCommandFieldCheckException;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽象的header 传输信息
 */
public class AbstractRemotingCommandBody implements RemotingCommandBody {

	private static final long serialVersionUID = -8184979792935677160L;

    /**
     * NodeType 的字符串表示, 节点类型
     */
    @NotNull
    private String nodeType;

    /**
     * 当前节点的唯一标识
     */
    @NotNull
    private String identity;

    private Long timestamp = SystemClock.now();

    // 额外的参数
    @Nullable
    private Map<String, Object> extParams;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getExtParams() {
        return extParams;
    }

    public void setExtParams(Map<String, Object> extParams) {
        this.extParams = extParams;
    }

    public void putExtParam(String key, Object obj) {
        if (this.extParams == null) {
            this.extParams = new HashMap<String, Object>();
        }
        this.extParams.put(key, obj);
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    @Override
    public void checkFields() throws RemotingCommandFieldCheckException {

    }
}
