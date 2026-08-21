import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/*
bge-m3 kullanım sebepleri şu şekilde sıralanabilir:
1- 100'den fazla dili destekler.
2- Kısa cümlelerden uzun makalelere kadar metinleri aynı kalitede vektörleştirebilir.
3- Vektör boyutu 1024'tür. Hem seyrek (kelime bazlı) hem de yoğun aramayı tek modelde birleştirir.

Diğer seçenekler nelerdir?
-nomic-embed-text: Bağlam kapasitesi yüksektir. İngilizce için optimize edilmiştir.
-mxbai-embed-large: İngilizce metinleri aramak ve RAG sistemleri kurmak için son derece başarılı, güçlü bir modeldir.
-all-minilm: Çok hafif, çok hızlı ve çok az RAM tüketen bir modeldir. Ancak vektör boyutu küçüktür (384) ve kapasitesi sınırlıdır.
Sadece temel düzeyde veya test amaçlı, İngilizce projelerde kullanılır.

Gelen cümleyi JSON formatına dönüştürüyoruz. Bunun temel sebebi ollama api standardının JSON olması.
Ollama server arka planda Go ve C/C++ dilleriyle çalışır. Veri alışverişi için evrensel bir yapı gerekir


 */

public class EmbeddingService {

	private static final String OLLAMA_EMBED_URL =
			"http://localhost:11434/api/embed";

	private static final String EMBEDDING_MODEL =
			"bge-m3";

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;

	public EmbeddingService() {
		this.httpClient = HttpClient.newHttpClient();
		this.objectMapper = new ObjectMapper();
	}

	public List<Float> getEmbedding(String sentence)
			throws IOException, InterruptedException {

		String jsonBody = "{"
				+ "\"model\":\"" + EMBEDDING_MODEL + "\","
				+ "\"input\":\"" + escapeJson(sentence) + "\""
				+ "}";

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(OLLAMA_EMBED_URL))
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(jsonBody))
				.build();

		HttpResponse<String> response = httpClient.send(
				request,
				HttpResponse.BodyHandlers.ofString()
		);

		if (response.statusCode() != 200) {
			throw new RuntimeException(
					"Ollama embedding hatası: "
							+ response.statusCode()
							+ " - "
							+ response.body()
			);
		}

		JsonNode root = objectMapper.readTree(response.body());

		JsonNode embeddingNode = root
				.get("embeddings")
				.get(0);

		List<Float> embedding = new ArrayList<>();

		for (JsonNode value : embeddingNode) {
			embedding.add((float) value.asDouble());
		}

		return embedding;
	}

	private String escapeJson(String text) {
		return text
				.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\n", "\\n")
				.replace("\r", "\\r")
				.replace("\t", "\\t");
	}
}