package util;

import java.util.List;
import java.util.Map;


public class TableChunk {

    private final String tableName;
    private final List<Map<String, Object>> rows;

    public TableChunk(String tableName, List<Map<String, Object>> rows) {
        this.tableName = tableName;
        this.rows = rows;
    }

    public String getTableName() {
        return tableName;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }
}