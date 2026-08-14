package nlp;

import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tr.TurkishAnalyzer;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class CleanStopWord {

    public String removeStopWords(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        List<String> tokens = new ArrayList<>();
        CharArraySet stopSet = TurkishAnalyzer.getDefaultStopSet();

        StandardTokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(new StringReader(text));

        try (TokenStream tokenStream = new StopFilter(new TurkishLowerCaseFilter(tokenizer), stopSet)) {
            CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
            tokenStream.reset();

            while (tokenStream.incrementToken()) {
                tokens.add(attr.toString());
            }

            tokenStream.end();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return String.join(" ", tokens);
    }
}