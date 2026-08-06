package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.neo4j.driver.Driver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseEdge {
	
	 private final ObjectMapper mapper = new ObjectMapper();

	    private final AddEdge addEdge;

	    public ParseEdge(Driver driver) {
	        this.addEdge = new AddEdge(driver);
	    }

	    public void parse() {

	        try (BufferedReader reader =
	                new BufferedReader(new FileReader("output.ndjson"))) {

	            String line;

	            while ((line = reader.readLine()) != null) {

	                JsonNode json = mapper.readTree(line);

	                if (!"EDGE".equals(json.path("entityType").asText())) {
	                    continue;
	                }

	                String id = json.path("edge").path("id").asText();

	                String type = json.path("edge").path("type").asText();
	                
	                String source = json.path("edge").path("source").asText();
	                
	                String target = json.path("edge").path("target").asText();

	                Map<String, Object> properties =mapper.convertValue(json.path("edge").path("properties"),new TypeReference<Map<String, Object>>() {});

	                addEdge.addEdge(id, type, source, target, properties);

	            }

	        } catch (IOException e) {
	            e.printStackTrace();
	        }

	    }

}
