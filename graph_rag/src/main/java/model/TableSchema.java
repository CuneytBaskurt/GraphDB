package model;

import java.util.ArrayList;
import java.util.List;

public class TableSchema {
	
	private final String tableName;
    private final List<String> primaryKeyColumns = new ArrayList<>();  
    private final List<ForeignKeyInfo> foreignKeys = new ArrayList<>();
    private boolean junctionTable = false;  

    public TableSchema(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    public void addPrimaryKeyColumn(String column) {
        primaryKeyColumns.add(column);
    }

    public List<ForeignKeyInfo> getForeignKeys() {
        return foreignKeys;
    }

    public void addForeignKey(ForeignKeyInfo fk) {
        foreignKeys.add(fk);
    }

    public boolean isJunctionTable() {
        return junctionTable;
    }

    public void setJunctionTable(boolean junctionTable) {
        this.junctionTable = junctionTable;
    }

    public boolean hasCompositePrimaryKey() {
        return primaryKeyColumns.size() > 1;
    }

    @Override
    public String toString() {
        return "TableSchema{" +
                "table=" + tableName +
                ", pk=" + primaryKeyColumns +
                ", fks=" + foreignKeys +
                ", junction=" + junctionTable +
                '}';
    }

}
