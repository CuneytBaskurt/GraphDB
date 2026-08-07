package util;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.Values;

public class DeleteEdge {

    private final Driver driver;

    public DeleteEdge(Driver driver) {
        this.driver = driver;
    }

    public void delete(String id) {

        String cypher =
                "MATCH ()-[r {id:$id}]-() " +
                "DELETE r";

        try (Session session = driver.session()) {
            session.run(cypher, Values.parameters("id", id));
        }

    }

}