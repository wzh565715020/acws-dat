package com.tyyd.framework.dat.management.access.memory;


import java.util.List;

import com.tyyd.framework.dat.core.cluster.Node;
import com.tyyd.framework.dat.core.cluster.NodeType;
import com.tyyd.framework.dat.core.commons.utils.CharacterUtils;
import com.tyyd.framework.dat.core.commons.utils.CollectionUtils;
import com.tyyd.framework.dat.core.commons.utils.StringUtils;
import com.tyyd.framework.dat.core.logger.Logger;
import com.tyyd.framework.dat.core.logger.LoggerFactory;
import com.tyyd.framework.dat.management.access.RshHandler;
import com.tyyd.framework.dat.management.request.NodePaginationReq;
import com.tyyd.framework.dat.store.jdbc.builder.DeleteSql;
import com.tyyd.framework.dat.store.jdbc.builder.InsertSql;
import com.tyyd.framework.dat.store.jdbc.builder.SelectSql;
import com.tyyd.framework.dat.store.jdbc.builder.WhereSql;
import com.tyyd.framework.dat.store.jdbc.dbutils.JdbcTypeUtils;
import com.tyyd.framework.dat.store.jdbc.builder.OrderByType;

public class NodeMemCacheAccess extends MemoryAccess {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeMemCacheAccess.class);

    public NodeMemCacheAccess() {
        createTable(readSqlFile("sql/h2/lts_node.sql"));
    }

    private String getTableName() {
        return "lts_node";
    }

    public void addNode(List<Node> nodes) {
        for (Node node : nodes) {
            try {
                NodePaginationReq request = new NodePaginationReq();
                request.setIdentity(node.getIdentity());
                List<Node> existNodes = search(request);
                if (CollectionUtils.isNotEmpty(existNodes)) {
                    // 如果存在,那么先删除
                    removeNode(existNodes);
                }

                new InsertSql(getSqlTemplate())
                        .insert(getTableName())
                        .columns("identity",
                                "available",
                                "cluster_name",
                                "node_type",
                                "ip",
                                "port",
                                "node_group",
                                "create_time",
                                "threads",
                                "host_name",
                                "http_cmd_port"
                        )
                        .values(node.getIdentity(),
                                node.isAvailable() ? 1 : 0,
                                node.getClusterName(),
                                node.getNodeType().name(),
                                node.getIp(),
                                node.getPort(),
                                node.getGroup(),
                                node.getCreateTime(),
                                node.getThreads(),
                                node.getHostName(),
                                node.getHttpCmdPort())
                        .doInsert();
            } catch (Exception e) {
                LOGGER.error("Insert {} error!", node, e);
            }
        }
    }

    public void clear() {
        new DeleteSql(getSqlTemplate())
                .delete()
                .from()
                .table(getTableName())
                .doDelete();
    }

    public void removeNode(List<Node> nodes) {
        for (Node node : nodes) {
            try {
                new DeleteSql(getSqlTemplate())
                        .delete()
                        .from()
                        .table(getTableName())
                        .where("identity = ?", node.getIdentity())
                        .doDelete();
            } catch (Exception e) {
                LOGGER.error("Delete {} error!", node, e);
            }
        }
    }

    public Node getNodeByIdentity(String identity){
       return new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .where("identity = ?", identity)
                .single(RshHandler.NODE_RSH);
    }

    public List<Node> getNodeByNodeType(NodeType nodeType){
        NodePaginationReq nodePaginationReq = new NodePaginationReq();
        nodePaginationReq.setNodeType(nodeType);
        return search(nodePaginationReq);
    }

    public List<Node> search(NodePaginationReq request) {

        WhereSql whereSql = new WhereSql()
                .andOnNotEmpty("identity = ?", request.getIdentity())
                .andOnNotEmpty("node_group = ?", request.getNodeGroup())
                .andOnNotNull("node_type = ?", request.getNodeType() == null ? null : request.getNodeType().name())
                .andOnNotEmpty("ip = ?", request.getIp())
                .andOnNotNull("available = ?", request.getAvailable())
                .andBetween("create_time", JdbcTypeUtils.toTimestamp(request.getStartDate()), JdbcTypeUtils.toTimestamp(request.getEndDate()));

        SelectSql selectSql = new SelectSql(getSqlTemplate())
                .select()
                .all()
                .from()
                .table(getTableName())
                .whereSql(whereSql);
        if (StringUtils.isNotEmpty(request.getField())) {
            selectSql.orderBy()
                    .column(CharacterUtils.camelCase2Underscore(request.getField()), OrderByType.convert(request.getDirection()));
        }
        return selectSql.limit(request.getStart(), request.getLimit())
                .list(RshHandler.NODE_LIST_RSH);
    }
}
