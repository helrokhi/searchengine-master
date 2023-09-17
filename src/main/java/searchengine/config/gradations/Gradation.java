package searchengine.config.gradations;

import lombok.AllArgsConstructor;
import searchengine.config.sitemaps.Page;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.utils.gradations.GradationCollectLemmas;
import searchengine.utils.methods.Methods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class Gradation extends Thread {
    private final Page page;
    private final Methods methods;
    private final GradationCollectLemmas gradationCollectLemmas;

    @Override
    public void run() {
        Map<String, Integer> wordsMap = gradationCollectLemmas.getLemmasMap(page);
        Collection<String> words = wordsMap.keySet();
        List<LemmaEntity> oldLemmaList = gradationCollectLemmas.getOldLemmaList(page.getSiteId(), words);
        Collection<String> lemmas = gradationCollectLemmas.getLemmas(page.getSiteId(), words);

        methods.incrementFrequencyAllLemmasEntity(oldLemmaList);

        List<LemmaEntity> newLemmaList = new ArrayList<>(0);
        if (words.size() > oldLemmaList.size()) {
            newLemmaList = gradationCollectLemmas.getNewLemmaList(page, wordsMap, lemmas);
            methods.saveAllNewLemmas(newLemmaList);
        }

        List<IndexEntity> indexList = gradationCollectLemmas.getIndexList(page, wordsMap);
        methods.saveAllIndexEntity(indexList);
    }
}
