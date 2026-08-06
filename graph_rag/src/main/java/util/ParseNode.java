package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.neo4j.driver.Driver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseNode {

    private final ObjectMapper mapper = new ObjectMapper();

    private final AddNode addNode;

    public ParseNode(Driver driver) {
        this.addNode = new AddNode(driver);
    }

    public void parse() {

        try (BufferedReader reader =
                new BufferedReader(new FileReader("output.ndjson"))) {

            String line;

            while ((line = reader.readLine()) != null) {

                JsonNode json = mapper.readTree(line);

                if (!"NODE".equals(json.path("entityType").asText())) {
                    continue;
                }

                String id = json.path("node").path("id").asText();

                String label = json.path("node").path("labels").get(0).asText();

                Map<String, Object> properties =mapper.convertValue(json.path("node").path("properties"),new TypeReference<Map<String, Object>>() {});

                addNode.addNode(id, label, properties);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}