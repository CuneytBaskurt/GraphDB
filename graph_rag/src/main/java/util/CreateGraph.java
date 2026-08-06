package util;

import graph_connect.Neo4jConnection;
import util.ParseEdge;
import util.ParseNode;

public class CreateGraph {

    private final Neo4jConnection neo;

    public CreateGraph() {
        neo = new Neo4jConnection();
    }

    public void create() {

        neo.connect();

        try {

            ParseNode nodeParser = new ParseNode(neo.getDriver());
            nodeParser.parse();

            ParseEdge edgeParser = new ParseEdge(neo.getDriver());
            edgeParser.parse();

        } finally {

            neo.close();

        }
    }

}