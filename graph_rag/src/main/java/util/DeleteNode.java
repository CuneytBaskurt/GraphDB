package util;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public class DeleteNode {

    private final Driver driver;

    public DeleteNode(Driver driver) {
        this.driver = driver;
    }

    public void delete(String id) {

        String cypher =
                "MATCH (n {id:$id}) " +
                "DETACH DELETE n";

        try (Session session = driver.session()) {
            session.run(cypher, Values.parameters("id", id));
        }
    }
}