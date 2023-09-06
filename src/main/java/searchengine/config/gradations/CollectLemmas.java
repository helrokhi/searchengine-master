package searchengine.config.gradations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CollectLemmas {
    private final CollectRuLemmas collectRuLemmas;
    private final CollectEnLemmas collectEnLemmas;

    @Autowired
    public CollectLemmas(CollectRuLemmas collectRuLemmas, CollectEnLemmas collectEnLemmas) {
        this.collectRuLemmas = collectRuLemmas;
        this.collectEnLemmas = collectEnLemmas;
    }

    public Map<String, Integer> collectLemmas(String text) {
        Map<String, Integer> lemmas = new HashMap<>(collectRuLemmas.collectLemmas(text));
        lemmas.putAll(collectEnLemmas.collectLemmas(text));
        return lemmas;
    }

    public Set<String> getLemmaSet(String text) {
        Set<String> lemmaSet = new HashSet<>(collectRuLemmas.getLemmaSet(text));
        lemmaSet.addAll(collectEnLemmas.getLemmaSet(text));
        return lemmaSet;
    }

    public List<String> getAllWordForms(String word) {
        List<String> allWordForms = collectRuLemmas.getAllWordForms(word);
        allWordForms.addAll(collectEnLemmas.getAllWordForms(word));
        return allWordForms;
    }
}
