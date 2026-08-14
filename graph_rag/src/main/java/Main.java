import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graph_connect.Neo4jConnection;
import model.TableSchema;
import nlp.NormalizeSentence;
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

    	
    	CreateGraph graph = new CreateGraph();
    	graph.create();
    	
    }
}
    	
    	
    

    

