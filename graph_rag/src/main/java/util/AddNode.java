package util;

import java.util.List;
import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Values;

public class AddNode {

    private final Driver driver;

    public AddNode(Driver driver) {
        this.driver = driver;
    }

    public void add(String id, String label, Map<String, Object> properties) {

        String cypher ="MERGE (n:" + label + " {id:$id}) " + "SET n += $properties";

        try (Session session = driver.session()) {

            session.run(cypher, Values.parameters("id", id, "properties", properties));
        }

    }

    public void addBatch(String label, List<Map<String, Object>> rows) {

        if (rows == null || rows.isEmpty()) {
            return;
        }

        String cypher ="UNWIND $rows AS row " + "MERGE (n:" + label + " {id: row.id}) " + "SET n += row.properties";

        try (Session session = driver.session(SessionConfig.forDatabase("chinook"))) {

            session.run(cypher, Values.parameters("rows", rows));

        }

    }

}