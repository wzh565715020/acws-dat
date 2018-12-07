package com.tyyd.framework.dat.management.monitor.access.domain;

public class JVMThreadDataPo extends MDataPo {

    private Integer daemonThreadCount;

    private Integer threadCount;

    private Long totalStartedThreadCount;

    private Integer deadLockedThreadCount;

    private Double processCpuTimeRate;

    public Integer getDaemonThreadCount() {
        return daemonThreadCount;
    }

    public void setDaemonThreadCount(Integer daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    public Integer getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(Integer threadCount) {
        this.threadCount = threadCount;
    }

    public Long getTotalStartedThreadCount() {
        return totalStartedThreadCount;
    }

    public void setTotalStartedThreadCount(Long totalStartedThreadCount) {
        this.totalStartedThreadCount = totalStartedThreadCount;
    }

    public Integer getDeadLockedThreadCount() {
        return deadLockedThreadCount;
    }

    public void setDeadLockedThreadCount(Integer deadLockedThreadCount) {
        this.deadLockedThreadCount = deadLockedThreadCount;
    }

    public Double getProcessCpuTimeRate() {
        return processCpuTimeRate;
    }

    public void setProcessCpuTimeRate(Double processCpuTimeRate) {
        this.processCpuTimeRate = processCpuTimeRate;
    }
}
