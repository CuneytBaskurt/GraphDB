import java.util.List;

import org.neo4j.driver.Record;

public class Main {

	public static void main(String[] args) {

		EmbeddingService embeddingService =
				new EmbeddingService();

		Neo4jConnection neo4j =
				new Neo4jConnection();

		GeminiService gemini =
				new GeminiService("AQ.Ab8RN6JTGZteZrs6V9ZYjqQeCBIL-3fP6YHzOlWO-tX2XmBdxA");

		String question =
				"Which tracks by the artist Audioslave have the \"Protected MPEG-4 video file\" media type?";

		try {

			// 1. Sorunun embedding'i
			List<Float> embedding = embeddingService.getEmbedding(question);

			System.out.println("Embedding boyutu: "+ embedding.size());
			System.out.println(embedding);

			// 2. En yakın 3 relationship
			List<Record> nearestRelations = neo4j.findNearestRelations(embedding);

			// 3. 2-hop subgraph
			String graphJson = neo4j.getSubgraphAsJson(nearestRelations);

			System.out.println("\n===== GRAPH JSON =====");

			System.out.println(graphJson);

			// 4. Gemini
			String answer = gemini.generateAnswer(question,graphJson);

			System.out.println("\n===== GEMINI CEVABI =====");

			System.out.println(answer);

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			neo4j.close();
		}
	}
}