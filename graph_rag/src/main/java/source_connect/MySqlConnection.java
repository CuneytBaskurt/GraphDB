package source_connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigLoader;


public class MySqlConnection implements JdbcConnection {

    private static final Logger logger = LoggerFactory.getLogger(MySqlConnection.class);

    private Connection connection;
    
    static {
        DatabaseConnectionRegistry.register("MYSQL", MySqlConnection::new);
    }

    @Override
    public void connect(Map<String, String> config) {
    	
    	if (isConnected()) {
            logger.warn("Zaten aktif bir MySQL bağlantısı var, yeni bağlantı kurulmadan önce kapatılıyor.");
            disconnect();
        }
    	
        String host = getValue(config, "host", "mysql.host", "localhost");
        String port = getValue(config, "port", "mysql.port", "3306");
        String database = getValue(config, "database", "mysql.database", "");
        String username = getValue(config, "username", "mysql.username", "root");
        String password = getValue(config, "password", "mysql.password", "");

        String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useSSL=true&trustServerCertificate=true&allowPublicKeyRetrieval=true&serverTimezone=UTC", host, port, database);
        
        logger.info("MySQL veritabanına bağlanılıyor... URL: jdbc:mysql://{}:{}/{}", host, port, database);

        try {
            this.connection = DriverManager.getConnection(jdbcUrl, username, password);
            logger.info("MySQL veritabanı bağlantısı başarıyla sağlandı! Database: {}", database);

        } catch (SQLException e) {
            logger.error("MySQL bağlantısı kurulurken hata oluştu! Hata Kodu: {}, Mesaj: {}",
                    e.getErrorCode(), e.getMessage());
            throw new RuntimeException("MySQL bağlantısı kurulamadı: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() {
        if (isConnected()) {
            try {
                connection.close();
                logger.info("MySQL bağlantısı başarıyla kapatıldı.");
            } catch (SQLException e) {
                logger.warn("MySQL bağlantısı kapatılırken bir sorun oluştu: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("Kapatılmaya çalışılan MySQL bağlantısı zaten kapalı veya hiç kurulmamış.");
        }
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.error("Bağlantı durumu kontrol edilirken hata oluştu: {}", e.getMessage());
            return false;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    private String getValue(Map<String, String> config, String key, String propertyKey, String defaultValue) {
        if (config != null) {
            String val = config.get(key);
            if (val != null && !val.trim().isEmpty()) {
                return val;
            }
        }
        return ConfigLoader.getProperty(propertyKey, defaultValue);
    }

}