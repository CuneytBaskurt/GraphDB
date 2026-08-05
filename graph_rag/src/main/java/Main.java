import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.TableSchema;
import source_connect.DatabaseConnection;
import source_connect.DatabaseFactory;
import source_connect.JdbcConnection;
import util.DatabaseChunkedReader;
import util.SchemaDiscoverer;
import util.TableChunk;

public class Main {

	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		
		AdapterInitializer.registerAll();

		DatabaseConnection conn = DatabaseFactory.createConnection("MYSQL");

		if (!(conn instanceof JdbcConnection)) {
		    throw new IllegalStateException("Bu kaynak tipi chunked reading desteklemiyor.");
		}
		JdbcConnection jdbcConn = (JdbcConnection) conn;

		Map<String, String> config = new HashMap<>();
		jdbcConn.connect(config);   
		
		SchemaDiscoverer discoverer = new SchemaDiscoverer();
		Map<String, TableSchema> schemas = discoverer.discoverSchema(jdbcConn.getConnection());

		for (TableSchema schema : schemas.values()) {
		    System.out.println(schema);
		}   
		
	} 

}
