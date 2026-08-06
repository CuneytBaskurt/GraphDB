package mapper;

import model.GraphNode;
import model.TableSchema;
import model.ForeignKeyInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RowToNodeMapper {

    public static GraphNode map(Map<String, Object> row, TableSchema schema, String tenantId) {
        if (schema.isJunctionTable()) {
            return null;
        }

        String pkValue = extractPkValue(row, schema.getPrimaryKeyColumns());
        if (pkValue == null || pkValue.isEmpty()) {
            return null;
        }

        String nodeId = tenantId + "::" + schema.getTableName() + "::" + pkValue;
        GraphNode node = new GraphNode(nodeId);

        // Label üretimi (Örn: customer -> Customer)
        node.addLabel(capitalize(schema.getTableName()));

        // Tablodaki tüm FK kolonlarının listesi
        Set<String> fkColumns = new HashSet<>();
        for (ForeignKeyInfo fk : schema.getForeignKeys()) {
            fkColumns.addAll(fk.getSourceColumns());
        }

        // Özniteliklerin yüklenmesi (PK ve FK hariç tutulur)
        for (Map.Entry<String, Object> entry : row.entrySet()) {
            String col = entry.getKey();
            Object val = entry.getValue();

            if (schema.getPrimaryKeyColumns().contains(col)) continue;
            if (fkColumns.contains(col)) continue;
            if (val == null) continue;

            node.putProperty(col, normalizeValue(val));
        }

        return node;
    }

    public static String extractPkValue(Map<String, Object> row, List<String> pkColumns) {
        if (pkColumns == null || pkColumns.isEmpty()) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pkColumns.size(); i++) {
            Object val = row.get(pkColumns.get(i));
            if (val == null) return null;
            if (i > 0) sb.append("::");
            sb.append(val.toString());
        }
        return sb.toString();
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static Object normalizeValue(Object val) {
        if (val == null) return null;
        if (val instanceof java.sql.Date || val instanceof java.sql.Timestamp || val instanceof java.sql.Time) {
            return val.toString();
        }
        if (val instanceof java.math.BigDecimal) {
            return ((java.math.BigDecimal) val).doubleValue();
        }
        return val;
    }
}