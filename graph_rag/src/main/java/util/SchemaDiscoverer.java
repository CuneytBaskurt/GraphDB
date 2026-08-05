package util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.ForeignKeyInfo;
import model.TableSchema;

public class SchemaDiscoverer {
	
    private static final Logger logger = LoggerFactory.getLogger(SchemaDiscoverer.class);
    
    public Map<String, TableSchema> discoverSchema(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();

            List<String> tableNames = discoverTableNames(metaData, catalog);
            Map<String, TableSchema> schemas = new LinkedHashMap<>();

            // 1. Faz: her tablo için PK bilgisini topla
            for (String tableName : tableNames) {
                TableSchema schema = new TableSchema(tableName);
                loadPrimaryKeys(metaData, catalog, tableName, schema);
                schemas.put(tableName, schema);
            }
            
            for (String tableName : tableNames) {
                loadForeignKeys(metaData, catalog, tableName, schemas.get(tableName));
            }

            // 3. Faz: junction tablo tespiti (PK+FK bilgisi tamamlandıktan sonra yapılabilir)
            for (TableSchema schema : schemas.values()) {
                detectJunctionTable(schema);
            }

            logger.info("Şema keşfi tamamlandı. {} tablo analiz edildi.", schemas.size());
            for (TableSchema schema : schemas.values()) {
                logger.debug("  {}", schema);
            }

            return schemas;

        } catch (SQLException e) {
            throw new RuntimeException("Şema keşfi sırasında hata oluştu: " + e.getMessage(), e);
        }
    }
    
    private List<String> discoverTableNames(DatabaseMetaData metaData, String catalog) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
        }
        return tables;
    }
    
    private void loadPrimaryKeys(DatabaseMetaData metaData, String catalog, String tableName, TableSchema schema)
            throws SQLException {
        // KEY_SEQ sırasına göre doğru dizilim garantisi için TreeMap kullanıyoruz
        Map<Short, String> orderedColumns = new TreeMap<>();
        try (ResultSet pks = metaData.getPrimaryKeys(catalog, null, tableName)) {
            while (pks.next()) {
                short keySeq = pks.getShort("KEY_SEQ");
                String columnName = pks.getString("COLUMN_NAME");
                orderedColumns.put(keySeq, columnName);
            }
        }
        for (String column : orderedColumns.values()) {
            schema.addPrimaryKeyColumn(column);
        }
    }
    
    private void loadForeignKeys(DatabaseMetaData metaData, String catalog, String tableName, TableSchema schema)
            throws SQLException {
        // FK_NAME -> (KEY_SEQ -> kolon çifti) şeklinde gruplama yapıyoruz
        Map<String, TreeMap<Short, String[]>> fkGroups = new LinkedHashMap<>();
        String targetTablePerFk = null;

        try (ResultSet fks = metaData.getImportedKeys(catalog, null, tableName)) {
            while (fks.next()) {
                String fkName = fks.getString("FK_NAME");
                // Bazı DB'ler (özellikle MySQL'de isimsiz kısıtlamalarda) FK_NAME null dönebilir,
                // bu durumda hedef tablo + sıra numarasından sentetik bir grup anahtarı üretiyoruz
                if (fkName == null) {
                    fkName = tableName + "_fk_" + fks.getString("PKTABLE_NAME") + "_" + fks.getShort("KEY_SEQ");
                }
                short keySeq = fks.getShort("KEY_SEQ");
                String sourceColumn = fks.getString("FKCOLUMN_NAME");
                String targetColumn = fks.getString("PKCOLUMN_NAME");
                String targetTable = fks.getString("PKTABLE_NAME");

                fkGroups.computeIfAbsent(fkName, k -> new TreeMap<>())
                        .put(keySeq, new String[]{sourceColumn, targetColumn, targetTable});
            }
        }
        for (Map.Entry<String, TreeMap<Short, String[]>> entry : fkGroups.entrySet()) {
            List<String> sourceCols = new ArrayList<>();
            List<String> targetCols = new ArrayList<>();
            String targetTable = null;

            for (String[] triplet : entry.getValue().values()) {
                sourceCols.add(triplet[0]);
                targetCols.add(triplet[1]);
                targetTable = triplet[2]; // her satırda aynı olmalı, sonuncusu kalır
            }

            schema.addForeignKey(new ForeignKeyInfo(targetTable, sourceCols, targetCols));
        }
    }
    
    private void detectJunctionTable(TableSchema schema) {
        if (schema.getForeignKeys().size() != 2) {
            return; // junction tablo tanımı gereği tam 2 FK olmalı
        }

        // İki FK'nin kapsadığı tüm kaynak kolonları birleştir
        List<String> fkColumnsCombined = new ArrayList<>();
        for (ForeignKeyInfo fk : schema.getForeignKeys()) {
            fkColumnsCombined.addAll(fk.getSourceColumns());
        }

        // PK'nın tamamı, bu FK kolonlarından oluşuyorsa (fazladan kolon yoksa)
        // bu tabloyu junction olarak işaretliyoruz
        boolean pkMatchesFkColumns = schema.getPrimaryKeyColumns().size() == fkColumnsCombined.size()
                && fkColumnsCombined.containsAll(schema.getPrimaryKeyColumns());

        if (pkMatchesFkColumns) {
            schema.setJunctionTable(true);
            logger.info("'{}' tablosu junction (köprü) tablo olarak tespit edildi.", schema.getTableName());
        }
    }

}
