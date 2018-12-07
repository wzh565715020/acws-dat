package com.tyyd.framework.dat.management.monitor;

import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.management.monitor.access.face.JVMGCAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JVMMemoryAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JVMThreadAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JobClientMAccess;
import com.tyyd.framework.dat.management.monitor.access.face.JobTrackerMAccess;
import com.tyyd.framework.dat.management.monitor.access.face.TaskTrackerMAccess;
import com.tyyd.framework.dat.management.monitor.cmd.MDataSrv;

public class MonitorAppContext extends AppContext {

    private int httpCmdPort;

    private JobTrackerMAccess jobTrackerMAccess;
    private TaskTrackerMAccess taskTrackerMAccess;
    private JobClientMAccess jobClientMAccess;
    private JVMGCAccess jvmGCAccess;
    private JVMMemoryAccess jvmMemoryAccess;
    private JVMThreadAccess jvmThreadAccess;

    private MDataSrv mDataSrv;

    public int getHttpCmdPort() {
        return httpCmdPort;
    }

    public void setHttpCmdPort(int httpCmdPort) {
        this.httpCmdPort = httpCmdPort;
    }

    public JobTrackerMAccess getJobTrackerMAccess() {
        return jobTrackerMAccess;
    }

    public void setJobTrackerMAccess(JobTrackerMAccess jobTrackerMAccess) {
        this.jobTrackerMAccess = jobTrackerMAccess;
    }

    public TaskTrackerMAccess getTaskTrackerMAccess() {
        return taskTrackerMAccess;
    }

    public void setTaskTrackerMAccess(TaskTrackerMAccess taskTrackerMAccess) {
        this.taskTrackerMAccess = taskTrackerMAccess;
    }

    public JVMGCAccess getJvmGCAccess() {
        return jvmGCAccess;
    }

    public void setJvmGCAccess(JVMGCAccess jvmGCAccess) {
        this.jvmGCAccess = jvmGCAccess;
    }

    public JVMMemoryAccess getJvmMemoryAccess() {
        return jvmMemoryAccess;
    }

    public void setJvmMemoryAccess(JVMMemoryAccess jvmMemoryAccess) {
        this.jvmMemoryAccess = jvmMemoryAccess;
    }

    public JVMThreadAccess getJvmThreadAccess() {
        return jvmThreadAccess;
    }

    public void setJvmThreadAccess(JVMThreadAccess jvmThreadAccess) {
        this.jvmThreadAccess = jvmThreadAccess;
    }

    public MDataSrv getMDataSrv() {
        return mDataSrv;
    }

    public void setMDataSrv(MDataSrv mDataSrv) {
        this.mDataSrv = mDataSrv;
    }

    public JobClientMAccess getJobClientMAccess() {
        return jobClientMAccess;
    }

    public void setJobClientMAccess(JobClientMAccess jobClientMAccess) {
        this.jobClientMAccess = jobClientMAccess;
    }

    public MDataSrv getmDataSrv() {
        return mDataSrv;
    }

    public void setmDataSrv(MDataSrv mDataSrv) {
        this.mDataSrv = mDataSrv;
    }
}
