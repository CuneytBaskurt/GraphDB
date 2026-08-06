import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdapterInitializer {
    private static final Logger logger = LoggerFactory.getLogger(AdapterInitializer.class);
    private static boolean initialized = false;

    //Veritabanı classlarındaki static metotları çağırıyoruz.
    public static synchronized void registerAll() {
        if (initialized) {
            logger.debug("Adapter'lar zaten kayıtlı, tekrar yüklenmiyor.");
            return;
        }
        try {
            Class.forName("source_connect.MySqlConnection");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Adapter kaydı sırasında hata oluştu", e);
        }
        initialized = true;
        logger.info("Tüm adapter'lar registry'ye kaydedildi.");
    }
}