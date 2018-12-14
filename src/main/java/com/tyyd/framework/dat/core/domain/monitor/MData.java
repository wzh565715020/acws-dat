package com.tyyd.framework.dat.core.domain.monitor;

/**
 * @author   on 8/30/15.
 */
public class MData {

    private Long timestamp;

    private JvmMData jvmMData;

    public JvmMData getJvmMData() {
        return jvmMData;
    }

    public void setJvmMData(JvmMData jvmMData) {
        this.jvmMData = jvmMData;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

}
