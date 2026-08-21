import com.fasterxml.jackson.databind.ObjectMapper;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Values;

import java.util.List;
import java.util.Map;

/*
findNearestRelations
--------------------
Cypher içerisinde yerleşik olan vektör aramasını kullanır. K-NN en yakın arama kullanılır.
K-NN alternatifleri şunlardır:
ANN
Full-Text Search / BM25

getSubgraphAsJson
-----------------
Bulunan en yakın 3 ilişkinin benzersiz id değerleri veritabanından çekilir.
id değeri listede olan ilişkilerin kaynak ve hedef düğümleri bulunur.
Bu düğümler bir listeye toplanır.
Düğümleri listeden teker teker çıkarılır ve 2-hop genişlenir.
Elde edilen bu graf yapısı doğrudan Cypher üzerinde JSON uyumlu Map'e dönüştürülür.
*/

public class Neo4jConnection {

	private final Driver driver;
	private final ObjectMapper objectMapper;

	public Neo4jConnection() {
		driver = GraphDatabase.driver(
				"bolt://localhost:7687",
				AuthTokens.basic("DATABASE", "PASSWORD")
		);

		objectMapper = new ObjectMapper();
	}

	public Driver getDriver() {
		return driver;
	}

	public void close() {
		driver.close();
	}

	public List<Record> findNearestRelations(List<Float> embedding) {
		try (Session session = driver.session(SessionConfig.forDatabase("DATABASE_NAME"))) {
			String cypher = """
                    CALL db.index.vector.queryRelationships(
                        'relations_embedding_index',
                        3,
                        $embedding
                    )
                    YIELD relationship, score
                    RETURN relationship, score
                    ORDER BY score DESC
                    """;

			return session.run(
					cypher,
					Values.parameters("embedding", embedding)
			).list();
		}
	}
/*
	public List<Record> findNearestRelations(List<Float> embedding) {
		try (Session session = driver.session(SessionConfig.forDatabase("chinook"))) {

			// 1. Veritabanındaki tüm vektör ilişki indekslerinin adlarını çekiyoruz
			String getIndexesCypher = """
                SHOW VECTOR INDEXES 
                YIELD name, entityType 
                WHERE entityType = 'RELATIONSHIP'
                RETURN name
                """;

			List<String> indexNames = session.run(getIndexesCypher)
					.list(r -> r.get("name").asString());

			if (indexNames.isEmpty()) {
				System.out.println("Uyarı: Veritabanında hiçbir ilişki vektör indeksi bulunamadı!");
				return List.of();
			}

			// 2. APOC kullanmadan, tüm indeksleri UNION ALL ile tek bir sorguda birleştiriyoruz
			StringBuilder unionSubqueries = new StringBuilder();
			for (int i = 0; i < indexNames.size(); i++) {
				String indexName = indexNames.get(i);
				unionSubqueries.append("""
                   CALL db.index.vector.queryRelationships('%s', 3, $embedding)
                   YIELD relationship, score
                   RETURN relationship, score
                   """.formatted(indexName));

				if (i < indexNames.size() - 1) {
					unionSubqueries.append("\nUNION ALL\n");
				}
			}

			// 3. Birleştirilmiş sonuçları skora göre sıralayıp en yüksek 3 ilişkiyi alıyoruz
			String finalCypher = """
                CALL {
                   %s
                }
                RETURN relationship, score
                ORDER BY score DESC
                LIMIT 3
                """.formatted(unionSubqueries.toString());

			return session.run(
					finalCypher,
					Values.parameters("embedding", embedding)
			).list();
		}
	}
*/
	public String getSubgraphAsJson(List<Record> nearestRelations) throws Exception {

		if (nearestRelations == null || nearestRelations.isEmpty()) {
			return "{}"; // Eğer ilişki bulunamazsa boş JSON dön.
		}

		try (Session session = driver.session(SessionConfig.forDatabase("chinook"))) {

			List<Long> relationshipIds = nearestRelations.stream()
					.map(record -> record.get("relationship").asRelationship().id())
					.toList();

			String cypher = """
                    MATCH (source)-[r]->(target)
                    WHERE id(r) IN $relationshipIds
                    WITH collect(DISTINCT source) + collect(DISTINCT target) AS startNodes
                    UNWIND startNodes AS startNode
                    
                    // 2-hop genişleme yapıyoruz ve path içindeki node/ilişkileri çıkartıyoruz
                    MATCH path = (startNode)-[*0..2]-(m)
                    UNWIND nodes(path) AS pathNode
                    UNWIND relationships(path) AS pathRel
                    
                    // Benzersiz düğüm ve ilişkileri tek bir listede topluyoruz
                    WITH collect(DISTINCT pathNode) AS rawNodes, 
                         collect(DISTINCT pathRel) AS rawRels
                    
                    // Veritabanı içinde JSON formatına (Map) çeviriyoruz
                    RETURN {
                        nodes: [n IN rawNodes | {
                            id: elementId(n),
                            labels: labels(n),
                            // 'embedding' hariç tutulur
                            properties: n{.*, embedding: null}
                        }],
                        relationships: [rel IN rawRels | {
                            id: elementId(rel),
                            type: type(rel),
                            source: elementId(startNode(rel)),
                            target: elementId(endNode(rel)),
                            // 'embedding' hariç tutulur
                            properties: rel{.*, embedding: null}
                        }]
                    } AS graphData
                    """;

			List<Record> records = session.run(
					cypher,
					Values.parameters("relationshipIds", relationshipIds)
			).list();

			if (records.isEmpty()) {
				return "{}";
			}

			// Neo4j'nin ürettiği 'graphData' sözlüğünü (Map) doğrudan Java Map objesi olarak alıyoruz
			Map<String, Object> graphData = records.get(0).get("graphData").asMap();

			// Jackson ObjectMapper bu standart Map'i tek satırda kusursuz bir JSON'a dönüştürür.
			return objectMapper
					.writerWithDefaultPrettyPrinter()
					.writeValueAsString(graphData);
		}
	}
}