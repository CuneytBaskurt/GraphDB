package source_connect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseConnectionRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionRegistry.class);
    private static final Map<String, Supplier<DatabaseConnection>> registry = new HashMap<>();

    //Veritabanlarının static metotları ile gelip kendilerini kaydettirdikleri metot.
    public static void register(String dbType, Supplier<DatabaseConnection> supplier) {
        String key = normalize(dbType);
        if (registry.containsKey(key)) {
            logger.warn("'{}' türü zaten kayıtlı, üzerine yazılıyor.", key);
        }
        registry.put(key, supplier);
        logger.info("'{}' veritabanı adapter'ı registry'ye kaydedildi.", key);
    }

    //Veritabanı bağlantısını yarattığımız metot.
    public static DatabaseConnection create(String dbType) {
        Supplier<DatabaseConnection> supplier = registry.get(normalize(dbType));
        if (supplier == null) {
            throw new IllegalArgumentException(
                "Desteklenmeyen veritabanı türü: " + dbType + 
                ". Kayıtlı türler: " + registry.keySet());
        }
        return supplier.get();
    }

    public static boolean isRegistered(String dbType) {
        return registry.containsKey(normalize(dbType));
    }

    private static String normalize(String dbType) {
        if (dbType == null) {
            throw new IllegalArgumentException("Veritabanı türü null olamaz.");
        }
        return dbType.trim().toUpperCase();
    }
}