package source_connect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseFactory {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseFactory.class);

    public static DatabaseConnection createConnection(String dbType) {
        if (dbType == null || dbType.trim().isEmpty()) {
            logger.error("Veritabanı türü boş olamaz!");
            throw new IllegalArgumentException("Veritabanı türü belirtilmelidir.");
        }
        logger.info("{} bağlantı nesnesi örnekleniyor.", dbType);
        return DatabaseConnectionRegistry.create(dbType);
    }
}