import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graph_connect.Neo4jConnection;
import model.TableSchema;
import pipeline.ExtractionPipeline;
import source_connect.DatabaseConnection;
import source_connect.DatabaseFactory;
import source_connect.JdbcConnection;
import util.CreateGraph;
import util.DatabaseChunkedReader;
import util.ParseEdge;
import util.ParseNode;
import util.SchemaDiscoverer;
import writer.NdjsonWriter;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
    	
    	/**
        AdapterInitializer.registerAll(); //Tüm veritabanlarını sisteme kaydediyoruz
        
        DatabaseConnection conn = DatabaseFactory.createConnection("MYSQL"); //MySQL veritabanı bağlantısını oluşturuyoruz
        if (!(conn instanceof JdbcConnection)) {
            throw new IllegalStateException("Bu kaynak tipi chunked reading desteklemiyor.");
        }

        JdbcConnection jdbcConn = (JdbcConnection) conn; //jdbc kullanmamızın sebebi sql veritabanlarında sorgu yapma kolaylığı sağlaması
        jdbcConn.connect(new HashMap<>());

        try {
            // 1. Şema Keşfi
            SchemaDiscoverer discoverer = new SchemaDiscoverer();
            Map<String, TableSchema> schemas = discoverer.discoverSchema(jdbcConn.getConnection()); //PK, FK ve JunctionTable değerlerini toplar

            // 2. Parçalı Okuyucu ve Yazıcı
            DatabaseChunkedReader chunkReader = new DatabaseChunkedReader(jdbcConn, 500); //Verileri toplu değil parça parça okumak için
            
            try (NdjsonWriter writer = new NdjsonWriter("output.ndjson")) {
                // 3. Pipeline Orkestrasyonu
                ExtractionPipeline pipeline = new ExtractionPipeline(
                    chunkReader,
                    schemas,
                    "default_tenant",
                    "MYSQL",
                    writer
                );
                
                pipeline.execute();
            }

            logger.info("İşlem başarıyla sonlandı. 'output.ndjson' dosyası oluşturuldu.");

        } catch (Exception e) {
            logger.error("Ana akışta hata oluştu", e);
        } finally {
            jdbcConn.disconnect();
        }
    } **/
    	
    	CreateGraph graph = new CreateGraph();
    	graph.create();

    }
}