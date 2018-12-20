package com.tyyd.framework.dat.taskdispatch;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import com.tyyd.framework.dat.core.cluster.Node;

public class LeaderSelectorZkClient2 {

    //zookeeper服务器的地址
    private static final String     ZOOKEEPER_SERVER = "172.23.2.101:2181";


    public static void main(String[] args) throws Exception{
        //保存所有zkClient的列表

        try{
                //创建zkClient
                ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
                System.out.println(client.countChildren("/TYYD/DAT/TEST_CLUSTER/NODES/TASK_DISPATCH"));
                System.out.println(client.getChildren("/TYYD/DAT/TEST_CLUSTER/NODES/TASK_DISPATCH"));
                System.out.println(client.countChildren("/TYYD/DAT/TEST_CLUSTER/NODES/TASK_EXECUTER"));
                System.out.println(client.getChildren("/TYYD/DAT/TEST_CLUSTER/NODES/TASK_EXECUTER"));
                System.out.println(client.getChildren("/TYYD/DAT/MASTER/NODES/TASK_DISPATCH/MASTER"));
                System.out.println(client.getChildren("/TYYD/DAT/MASTER/NODES/TASK_DISPATCH/MASTER"));
                System.out.println(client.exists("/TYYD/DAT/TEST_CLUSTER/NODES/TASK_EXECUTER/TASK_EXECUTER:\\10.102.0.115:30007?clusterName=TEST_CLUSTER&threads=30&availableThreads=30&identity=execute2&createTime=1545294322845&httpCmdPort=8721"));
                System.out.println("节点为" + (Node)client.readData("/TYYD/DAT/MASTER/NODES/TASK_DISPATCH/MASTER"));
        }finally{
            System.out.println("Shutting down...");
        }
    }
}