package searchengine.config.gradations;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class CollectRuLemmas implements CollectLemmasInterface {
    private final LuceneMorphology russianLuceneMorphology;
    private final static String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private final static String RU_SYMBOL = "([^а-я\\s])";
    private final static String RU_WORD = "[а-я]+";

    @Autowired
    public CollectRuLemmas() {
        this.russianLuceneMorphology = russianLuceneMorphology();
    }

    private LuceneMorphology russianLuceneMorphology() {
        try {
            return new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, Integer> collectLemmas(String text) {
        return collectLemmas(text, RU_SYMBOL, russianLuceneMorphology);
    }

    public Set<String> getLemmaSet(String text) {
        return getLemmaSet(text, WORD_TYPE_REGEX, RU_SYMBOL, russianLuceneMorphology);
    }

    public List<String> getAllWordForms(String word) {
        return isRussianWord(word) ? getAllWordForms(word, russianLuceneMorphology) :
                new ArrayList<>(0);
    }

    private boolean isRussianWord(String word) {
        return word.matches(RU_WORD);
    }
}
