package util;

import java.util.Map;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public class AddNode {

    private final Driver driver;

    public AddNode(Driver driver) {
        this.driver = driver;
    }

    public void addNode(String id,String label,Map<String, Object> properties) {

        String cypher ="MERGE (n:" + label + " {id:$id}) " + "SET n += $properties";

        try (Session session = driver.session()) {
            session.run(cypher,Values.parameters("id", id,"properties", properties));
        }
    }
}