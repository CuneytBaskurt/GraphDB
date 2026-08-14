package nlp;


public class NormalizeSentence {
	
	public String normalize(String text) {
		
		CleanStopWord clean = new CleanStopWord();
		String lowerSentence = text.toLowerCase();
		String normalizedSentence = clean.removeStopWords(lowerSentence);
		
		return normalizedSentence;
	}

	
	
}
