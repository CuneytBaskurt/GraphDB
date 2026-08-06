package graph_connect;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import source_connect.MySqlConnection;

public class Neo4jConnection {
	
    private static final Logger logger = LoggerFactory.getLogger(Neo4jConnection.class);
    
    private Properties properties = new Properties();
    
    private Driver driver;
    
    public void connect() {
    	 ClassLoader loader = Thread.currentThread().getContextClassLoader();
    	 
    	 try (InputStream resourceStream =
                 loader.getResourceAsStream("application.properties")) {

        if (resourceStream == null) {
            throw new RuntimeException("application.properties bulunamadı.");
        }

        properties.load(resourceStream);

    	} catch (IOException e) {
        logger.error("Properties dosyası okunamadı.", e);
    	}
    	 
    	 
    	 String dbUri = properties.getProperty("neo4j.uri");
    	 String dbUser = properties.getProperty("neo4j.user");
    	 String dbPassword = properties.getProperty("neo4j.password");
    	 
    	 driver = GraphDatabase.driver(
    		        dbUri,
    		        AuthTokens.basic(dbUser, dbPassword)
    		);
    	 
    	 driver.verifyConnectivity();

    	 /*try (Session session = driver.session()) {
    	     session.run("CREATE (:Person {name:'Cüneyt'})");
    	     session.run("MATCH (n:Person {name: 'Cüneyt'}) SET n.age=22");
    	 }*/

    	 logger.info("Connection established."); 	 
    	 
    }
    
    public Driver getDriver() {
        return driver;
    }
    
    public void close() {
        if (driver != null) {
            driver.close();
            logger.info("Connection closed!");
        }
    }
}
