package util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import source_connect.JdbcConnection;

public class DatabaseChunkedReader {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseChunkedReader.class);

    private final JdbcConnection dbConnection;
    private final int chunkSize;

    private List<String> tableNames;
    private int currentTableIndex = -1;
    private long currentOffset = 0;
    private boolean allFinished = false;

    public DatabaseChunkedReader(JdbcConnection dbConnection, int chunkSize) {
        if (dbConnection == null) {
            throw new IllegalArgumentException("dbConnection null olamaz.");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize sıfırdan büyük olmalıdır.");
        }
        this.dbConnection = dbConnection;
        this.chunkSize = chunkSize;
    }

    public void open() {
        if (!dbConnection.isConnected()) {
            throw new IllegalStateException(
                "Bağlantı açık değil. open() çağrılmadan önce dbConnection.connect(config) çalıştırılmalı.");
        }

        this.tableNames = discoverTables(dbConnection.getConnection());
        logger.info("Veritabanında {} tablo bulundu: {}", tableNames.size(), tableNames);

        if (!tableNames.isEmpty()) {
            currentTableIndex = 0;
            currentOffset = 0;
        } else {
            allFinished = true;
        }
    }

    private List<String> discoverTables(Connection connection) {
        List<String> tables = new ArrayList<>();
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Tablo listesi alınamadı: " + e.getMessage(), e);
        }
        return tables;
    }

    public TableChunk nextChunk() {
        if (allFinished) {
            return null;
        }
        if (!dbConnection.isConnected()) {
            throw new IllegalStateException("Bağlantı kapanmış görünüyor, chunk okunamıyor.");
        }

        while (true) {
            String tableName = tableNames.get(currentTableIndex);
            List<Map<String, Object>> rows = readChunk(tableName, currentOffset);
            if (rows.isEmpty()) {
                if (!moveToNextTable()) {
                    allFinished = true;
                    return null;
                }
                continue;
            }

            currentOffset += rows.size();
            if (rows.size() < chunkSize) {
                if (!moveToNextTable()) {
                    allFinished = true;
                }
            }

            return new TableChunk(tableName, rows);
        }
    }

    private boolean moveToNextTable() {
        currentTableIndex++;
        currentOffset = 0;
        return currentTableIndex < tableNames.size();
    }

    private List<Map<String, Object>> readChunk(String tableName, long offset) {
        String sql = "SELECT * FROM `" + tableName + "` LIMIT ? OFFSET ?";
        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, chunkSize);
            stmt.setLong(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            logger.error("'{}' tablosu okunurken hata oluştu. Offset: {}, Hata: {}", tableName, offset, e.getMessage());
            throw new RuntimeException("Chunk okunamadı: " + e.getMessage(), e);
        }

        return rows;
    }

    public void close() {
        this.tableNames = null;
        this.currentTableIndex = -1;
        this.currentOffset = 0;
        this.allFinished = true;
        logger.info("DatabaseChunkedReader kapatıldı (bağlantı yaşam döngüsü çağırana ait).");
    }
}