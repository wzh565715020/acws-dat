package com.tyyd.framework.dat.core.registry;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;

/**
 *         <p/>
 *         /DAT/{集群名字}/NODES/TASK_EXECUTER/TASK_EXECUTER:\\192.168.0.150:8888?group=TASK_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         /DAT/{集群名字}/NODES/TASK_CLIENT/TASK_CLIENT:\\192.168.0.150:8888?group=JOB_CLIENT&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         /DAT/{集群名字}/NODES/TASK_DISPATCHER/TASK_DISPATCHER:\\192.168.0.150:8888?group=JOB_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true&listenNodeTypes=CLIENT,TASK_TRACKER
 *         /DAT/{集群名字}/NODES/MONITOR/MONITOR:\\192.168.0.150:8888?group=JOB_TRACKER&threads=8&identity=85750db6-e854-4eb3-a595-9227a5f2c8f6&createTime=1408189898185&isAvailable=true
 *         <p/>
 */
public class NodeRegistryUtils {

    public static String getRootPath(String clusterName) {
        return "/TYYD/DAT/" + clusterName + "/NODES";
    }

    public static String getNodeTypePath(String clusterName, NodeType nodeType) {
        return NodeRegistryUtils.getRootPath(clusterName) + "/" + nodeType;
    }

    public static Node parse(String fullPath) {
        Node node = new Node();
        String[] nodeDir = fullPath.split("/");
        NodeType nodeType = NodeType.valueOf(nodeDir[5]);
        node.setNodeType(nodeType);
        String url = nodeDir[6];

        url = url.substring(nodeType.name().length() + 3);
        String address = url.split("\\?")[0];
        String ip = address.split(":")[0];

        node.setIp(ip);
        if (address.contains(":")) {
            String port = address.split(":")[1];
            if (port != null && !"".equals(port.trim())) {
                node.setPort(Integer.valueOf(port));
            }
        }
        String params = url.split("\\?")[1];

        String[] paramArr = params.split("&");
        for (String paramEntry : paramArr) {
            String key = paramEntry.split("=")[0];
            String value = paramEntry.split("=")[1];
            if ("clusterName".equals(key)) {
                node.setClusterName(value);
            } else if ("threads".equals(key)) {
                node.setThreads(Integer.valueOf(value));
            } else if("availableThreads".equals(key)) {
            	node.setAvailableThreads(Integer.valueOf(value));
            }else if ("identity".equals(key)) {
                node.setIdentity(value);
            } else if ("createTime".equals(key)) {
                node.setCreateTime(Long.valueOf(value));
            } else if ("httpCmdPort".equals(key)) {
                node.setHttpCmdPort(Integer.valueOf(value));
            }
        }
        return node;
    }

    public static String getFullPath(Node node) {
        StringBuilder path = new StringBuilder();

        path.append(getRootPath(node.getClusterName()))
                .append("/")
                .append(node.getNodeType())
                .append("/")
                .append(node.getNodeType())
                .append(":\\\\")
                .append(node.getIp());

        if (node.getPort() != null && node.getPort() != 0) {
            path.append(":").append(node.getPort());
        }

        path.append("?")
                .append("clusterName=")
                .append(node.getClusterName());
        if (node.getThreads() != 0) {
            path.append("&threads=")
                    .append(node.getThreads());
        }
        if (node.getAvailableThreads() != 0) {
            path.append("&availableThreads=")
                    .append(node.getAvailableThreads());
        }
        path.append("&identity=")
                .append(node.getIdentity())
                .append("&createTime=")
                .append(node.getCreateTime());
        if (node.getHttpCmdPort() != null) {
            path.append("&httpCmdPort=").append(node.getHttpCmdPort());
        }

        return path.toString();
    }

    public static String getRealRegistryAddress(String registryAddress) {
        if (StringUtils.isEmpty(registryAddress)) {
            throw new IllegalArgumentException("registryAddress is null！");
        }
        if (registryAddress.startsWith("zookeeper://")) {
            return registryAddress.replace("zookeeper://", "");
        } else if (registryAddress.startsWith("redis://")) {
            return registryAddress.replace("redis://", "");
        } else if (registryAddress.startsWith("multicast://")) {
            return registryAddress.replace("multicast://", "");
        }
        throw new IllegalArgumentException("Illegal registry protocol");
    }

}
