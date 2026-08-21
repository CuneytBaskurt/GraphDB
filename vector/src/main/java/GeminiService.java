import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiService {

	private final Client client;

	private static final String MODEL = "gemini-3.6-flash";

	public GeminiService(String apiKey) {
		client = Client.builder()
				.apiKey(apiKey)
				.build();
	}

	public String generateAnswer(
			String question,
			String graphJson) {

		String prompt = """
                You are a question answering system that uses a Neo4j knowledge graph.

                Answer the user's question using ONLY the information
                contained in the provided graph JSON.

                Do not use external knowledge.
                Do not invent information.
                If the answer cannot be found in the graph,
                clearly state that the information is not available
                in the provided graph.

                User Question:
                %s

                Graph JSON:
                %s

                Answer the question concisely.
                """.formatted(question, graphJson);

		GenerateContentResponse response =
				client.models.generateContent(
						MODEL,
						prompt,
						null
				);

		return response.text();
	}
}