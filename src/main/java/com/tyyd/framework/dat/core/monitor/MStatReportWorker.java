package com.tyyd.framework.dat.core.monitor;

import com.tyyd.framework.dat.cmd.DefaultHttpCmd;
import com.tyyd.framework.dat.cmd.HttpCmd;
import com.tyyd.framework.dat.cmd.HttpCmdClient;
import com.tyyd.framework.dat.cmd.HttpCmdResponse;
import com.tyyd.framework.dat.core.AppContext;
import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.cmd.HttpCmdNames;
import com.tyyd.framework.dat.core.cmd.HttpCmdParamNames;
import com.tyyd.framework.dat.core.commons.utils.BatchUtils;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.domain.monitor.MData;
import com.tyyd.framework.dat.core.domain.monitor.MNode;
import com.tyyd.framework.dat.core.json.JSON;
import com.tyyd.framework.dat.core.loadbalance.LoadBalance;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.core.support.SystemClock;
import com.tyyd.framework.dat.jvmmonitor.JVMCollector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Robert HG (254963746@qq.com) on 3/11/16.
 */
public class MStatReportWorker implements Runnable {

    protected final Logger LOGGER = LoggerFactory.getLogger(MStatReportWorker.class);

    private int interval = 1;    // 1分钟
    private Integer preMinute = null;  // 上一分钟
    private AppContext appContext;
    private AbstractMStatReporter reporter;
    // 这里面保存发送失败的，不过有个最大限制，防止内存爆掉
    private List<MData> mDataQueue = new ArrayList<MData>();
    private final static int MAX_RETRY_RETAIN = 500;
    private final static int BATCH_REPORT_SIZE = 10;
    private volatile boolean running = false;
    private LoadBalance loadBalance;

    public MStatReportWorker(AppContext appContext, AbstractMStatReporter reporter) {
        this.appContext = appContext;
        this.reporter = reporter;
        interval = appContext.getConfig().getParameter("lts.monitor.report.interval", 1);
        this.loadBalance = ServiceLoader.load(LoadBalance.class, appContext.getConfig(), "monitor.select.loadbalance");
    }

    @Override
    public void run() {
        if (running) {
            return;
        }
        running = true;

        try {
            Calendar calendar = Calendar.getInstance();
            int minute = calendar.get(Calendar.MINUTE);
            if (preMinute == null) {
                preMinute = minute;
                return;
            }

            int diff = minute - preMinute;
            diff = diff < 0 ? diff + 60 : diff;
            if (diff != 0 && diff % interval == 0) {
                try {
                    // 变化超过了间隔时间，要立马收集
                    MData mData = reporter.collectMData();
                    long seconds = SystemClock.now() / 1000;
                    seconds = seconds - (seconds % 60);        // 所有都向下取整，保证是60的倍数
                    seconds = seconds - interval * 60;        // 算其实时间点的数据
                    mData.setTimestamp(seconds * 1000);
                    // JVM monitor
                    mData.setJvmMData(JVMCollector.collect());
                    // report
                    report(mData);

                } finally {
                    preMinute = minute;
                }
            }

        } catch (Throwable t) {
            LOGGER.error("MStatReportWorker collect failed.", t);
        } finally {
            running = false;
        }
    }

    private void report(MData mData) {
        // Send monitor data
        mDataQueue.add(mData);

        List<Node> monitorNodes = appContext.getSubscribedNodeManager().getNodeList(NodeType.MONITOR);
        if (CollectionUtils.isEmpty(monitorNodes)) {
            LOGGER.info("Please start monitor");
            return;
        }

        int toIndex = 0;
        int size = mDataQueue.size();
        try {
            for (int i = 0; i <= size / BATCH_REPORT_SIZE; i++) {
                List<MData> mDatas = BatchUtils.getBatchList(i, BATCH_REPORT_SIZE, mDataQueue);
                if (CollectionUtils.isNotEmpty(mDatas)) {
                    try {
                        HttpCmd cmd = new DefaultHttpCmd();
                        cmd.setCommand(HttpCmdNames.HTTP_CMD_ADD_M_DATA);
                        cmd.addParam(HttpCmdParamNames.M_NODE, JSON.toJSONString(buildMNode()));
                        cmd.addParam(HttpCmdParamNames.M_DATA, JSON.toJSONString(mDatas));

                        if (sendReq(monitorNodes, cmd)) {
                            toIndex = toIndex + CollectionUtils.sizeOf(mDatas);
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Report monitor data Error : " + e.getMessage());
                        break;
                    }
                }
            }
        } finally {
            // to delete
            if (toIndex == 0) {
                // do nothing
            } else if (size == toIndex) {
                mDataQueue.clear();
            } else {
                mDataQueue = mDataQueue.subList(toIndex + 1, size);
            }
        }
        // check size
        size = mDataQueue.size();
        if (size > MAX_RETRY_RETAIN) {
            // delete the oldest
            mDataQueue = mDataQueue.subList(size - MAX_RETRY_RETAIN, size);
        }
    }

    // 发送请求
    private boolean sendReq(List<Node> monitorNodes, HttpCmd cmd) {
        while (true) {
            Node node = selectMNode(monitorNodes);
            try {
                cmd.setNodeIdentity(node.getIdentity());
                HttpCmdResponse response = HttpCmdClient.doPost(node.getIp(), node.getPort(), cmd);
                if (response.isSuccess()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Report Monitor Data Success.");
                    }
                    return true;
                } else {
                    LOGGER.warn("Report Monitor Data Failed: " + response.getMsg());
                    monitorNodes.remove(node);
                }
            } catch (Exception e) {
                LOGGER.warn("Report Monitor Data Error: " + e.getMessage(), e);
                // 重试下一个
            }
            if (monitorNodes.size() == 0) {
                return false;
            }
        }
    }

    private Node selectMNode(List<Node> monitorNodes) {
        return loadBalance.select(monitorNodes, appContext.getConfig().getIdentity());
    }

    private MNode buildMNode() {
        MNode mNode = new MNode();
        mNode.setNodeType(reporter.getNodeType());
        mNode.setIdentity(appContext.getConfig().getIdentity());
        return mNode;
    }
}
