package searchengine.config.gradations;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class CollectEnLemmas
        implements CollectLemmasInterface {
    private final LuceneMorphology englishLuceneMorphology;
    private final static String WORD_TYPE_REGEX = "\\W\\w&&[^a-zA-Z\\s]";
    private final static String EN_SYMBOL = "([^a-z\\s])";
    private final static String EN_WORD = "[a-z]+";

    @Autowired
    public CollectEnLemmas() {
        this.englishLuceneMorphology = englishLuceneMorphology();
    }

    private LuceneMorphology englishLuceneMorphology() {
        try {
            return new EnglishLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Integer> collectLemmas(String text) {
        return collectLemmas(text, EN_SYMBOL, englishLuceneMorphology);
    }

    public Set<String> getLemmaSet(String text) {
        return getLemmaSet(text, WORD_TYPE_REGEX, EN_SYMBOL, englishLuceneMorphology);
    }

    public List<String> getAllWordForms(String word) {
        return isEnglishWord(word) ? getAllWordForms(word, englishLuceneMorphology) :
                new ArrayList<>(0);
    }

    private boolean isEnglishWord(String word) {
        return word.matches(EN_WORD);
    }
}
