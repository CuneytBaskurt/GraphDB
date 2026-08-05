package mapper;

import model.GraphEdge;
import model.TableSchema;
import model.ForeignKeyInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RowToEdgeMapper {

    public static List<GraphEdge> map(Map<String, Object> row, TableSchema schema, String tenantId) {
        List<GraphEdge> edges = new ArrayList<>();

        if (schema.isJunctionTable()) {
            List<ForeignKeyInfo> fks = schema.getForeignKeys();
            if (fks.size() == 2) {
                ForeignKeyInfo fk1 = fks.get(0);
                ForeignKeyInfo fk2 = fks.get(1);

                String sourcePk = RowToNodeMapper.extractPkValue(row, fk1.getSourceColumns());
                String targetPk = RowToNodeMapper.extractPkValue(row, fk2.getSourceColumns());

                if (sourcePk != null && targetPk != null) {
                    String sourceId = tenantId + "::" + fk1.getTargetTable() + "::" + sourcePk;
                    String targetId = tenantId + "::" + fk2.getTargetTable() + "::" + targetPk;

                    String edgeType = schema.getTableName().toUpperCase();
                    String edgeId = sourceId + "->" + edgeType + "->" + targetId;

                    GraphEdge edge = new GraphEdge(edgeId, edgeType, sourceId, targetId);

                    // Junction tablonun ekstra kolonlarını edge property olarak ekle
                    Set<String> fkCols = new HashSet<>();
                    fkCols.addAll(fk1.getSourceColumns());
                    fkCols.addAll(fk2.getSourceColumns());

                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        String col = entry.getKey();
                        Object val = entry.getValue();
                        if (fkCols.contains(col) || schema.getPrimaryKeyColumns().contains(col)) continue;
                        if (val == null) continue;
                        edge.putProperty(col, RowToNodeMapper.normalizeValue(val));
                    }
                    edges.add(edge);
                }
            }
        } else {
            String sourcePk = RowToNodeMapper.extractPkValue(row, schema.getPrimaryKeyColumns());
            if (sourcePk == null) return edges;

            String sourceId = tenantId + "::" + schema.getTableName() + "::" + sourcePk;

            for (ForeignKeyInfo fk : schema.getForeignKeys()) {
                String targetPk = RowToNodeMapper.extractPkValue(row, fk.getSourceColumns());
                if (targetPk != null) {
                    String targetId = tenantId + "::" + fk.getTargetTable() + "::" + targetPk;
                    
                    String edgeType = "HAS_" + fk.getTargetTable().toUpperCase();
                    String edgeId = sourceId + "->" + edgeType + "->" + targetId;

                    GraphEdge edge = new GraphEdge(edgeId, edgeType, sourceId, targetId);
                    edges.add(edge);
                }
            }
        }

        return edges;
    }
}