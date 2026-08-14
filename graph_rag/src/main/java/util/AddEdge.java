package util;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Values;

public class AddEdge {

    private final Driver driver;

    public AddEdge(Driver driver) {
        this.driver = driver;
    }

    public void addEdge(String id, String type, String source,
                        String target, Map<String, Object> properties) {

        String cypher ="MATCH (source {id:$source}), (target {id:$target}) " + "MERGE (source)-[r:" + type + "]->(target) " + "SET r.id=$id " + "SET r += $properties";

        try (Session session = driver.session()) {

            session.run(cypher, Values.parameters("id", id,"source", source,"target", target,"properties", properties));
            
        }

    }

    public void addBatch(String type, List<Map<String, Object>> rows) {

        if (rows == null || rows.isEmpty()) {
            return;
        }

        String cypher ="UNWIND $rows AS row " + "MATCH (source {id: row.source}) " + "MATCH (target {id: row.target}) " + "MERGE (source)-[r:" + type + "]->(target) " + "SET r.id = row.id " + "SET r += row.properties";

        try (Session session = driver.session(SessionConfig.forDatabase("chinook"))) {

            session.run(
                    cypher,
                    Values.parameters("rows", rows)
            );

        }

    }

}