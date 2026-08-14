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

public class ParseEdge {

    private static final int BATCH_SIZE = 500;

    private final ObjectMapper mapper = new ObjectMapper();

    private final AddEdge addEdge;
    private final DeleteEdge deleteEdge;

    public ParseEdge(Driver driver) {

        this.addEdge = new AddEdge(driver);
        this.deleteEdge = new DeleteEdge(driver);

    }

    public void parse() {

        Map<String, List<Map<String, Object>>> batches = new HashMap<>();

        try (BufferedReader reader =
                new BufferedReader(new FileReader("output.ndjson"))) {

            String line;

            while ((line = reader.readLine()) != null) {

                JsonNode json = mapper.readTree(line);

                if (!"EDGE".equals(json.path("entityType").asText())) {
                    continue;
                }

                String operation = json.path("operation").asText();

                String id = json.path("edge").path("id").asText();

                if ("DELETE".equals(operation)) {
                    deleteEdge.delete(id);
                    continue;
                }

                String type = json.path("edge").path("type").asText();

                String source = json.path("edge").path("source").asText();

                String target = json.path("edge").path("target").asText();

                Map<String, Object> properties =
                        mapper.convertValue(
                                json.path("edge").path("properties"),
                                new TypeReference<Map<String, Object>>() {
                                });

                Map<String, Object> row = new HashMap<>();

                row.put("id", id);
                row.put("source", source);
                row.put("target", target);
                row.put("properties", properties);

                batches.computeIfAbsent(type, k -> new ArrayList<>());

                List<Map<String, Object>> batch = batches.get(type);

                batch.add(row);

                if (batch.size() >= BATCH_SIZE) {

                    addEdge.addBatch(type, batch);

                    batch.clear();

                }

            }

            for (Map.Entry<String, List<Map<String, Object>>> entry : batches.entrySet()) {

                if (!entry.getValue().isEmpty()) {

                    addEdge.addBatch(
                            entry.getKey(),
                            entry.getValue());

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}