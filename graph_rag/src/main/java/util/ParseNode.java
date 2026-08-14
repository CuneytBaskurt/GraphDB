package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseNode {

    private static final int BATCH_SIZE = 500;

    private final ObjectMapper mapper = new ObjectMapper();

    private final AddNode addNode;
    private final DeleteNode deleteNode;

    public ParseNode(Driver driver) {
        this.addNode = new AddNode(driver);
        this.deleteNode = new DeleteNode(driver);
    }

    public void parse() {

        Map<String, List<Map<String, Object>>> batches = new HashMap<>();//??

        try (BufferedReader reader = new BufferedReader(new FileReader("output.ndjson"))) {

            String line;

            while ((line = reader.readLine()) != null) {

                JsonNode json = mapper.readTree(line);

                if (!"NODE".equals(json.path("entityType").asText())) {
                    continue;
                }

                String operation = json.path("operation").asText();

                String id = json.path("node").path("id").asText();

                if ("DELETE".equals(operation)) {
                    deleteNode.delete(id);
                    continue;
                }

                String label = json.path("node").path("labels").get(0).asText();

                Map<String, Object> properties = mapper.convertValue(json.path("node").path("properties"), new TypeReference<Map<String, Object>>() {});

                Map<String, Object> row = new HashMap<>();
                row.put("id", id);
                row.put("properties", properties);

                batches.computeIfAbsent(label, k -> new ArrayList<>());

                List<Map<String, Object>> batch = batches.get(label);

                batch.add(row);

                if (batch.size() >= BATCH_SIZE) {

                    addNode.addBatch(label, batch);

                    batch.clear();
                }

            }

            //entry is batch
            for (Map.Entry<String, List<Map<String, Object>>> entry : batches.entrySet()) {

                if (!entry.getValue().isEmpty()) {

                    addNode.addBatch(entry.getKey(), entry.getValue());

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}