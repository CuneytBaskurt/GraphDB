package config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigLoader {
	
	 private static final Properties properties = new Properties();
	 private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
	 
	 static {
	        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
	            if (input == null) {
	                logger.warn("application.properties dosyası bulunamadı! Varsayılan değerler kullanılacak.");
	            } else {
	                properties.load(input);
	                logger.info("application.properties dosyası başarıyla yüklendi.");
	            }
	        } catch (IOException ex) {
	            logger.error("application.properties dosyası okunurken hata oluştu!", ex);
	        }
	    }

	    public static String getProperty(String key, String defaultValue) {
	        return properties.getProperty(key, defaultValue);
	    }

}
