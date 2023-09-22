package searchengine.dto.gradations;

import lombok.AllArgsConstructor;
import searchengine.utils.sitemaps.Page;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.utils.gradations.GradationCollectLemmas;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class Gradation extends Thread {
    private final Page page;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GradationCollectLemmas gradationCollectLemmas;

    @Override
    public void run() {
        Map<String, Integer> wordsMap = gradationCollectLemmas.getLemmasMap(page);
        Collection<String> words = wordsMap.keySet();
        List<LemmaEntity> oldLemmaList =
                lemmaRepository.findByLemmas(page.getSiteId(), words);
        Collection<String> lemmas = lemmaRepository.findAllLemmaInLemma(page.getSiteId(), words);

        incrementFrequencyAllLemmasEntity(oldLemmaList);

        List<LemmaEntity> newLemmaList;
        if (words.size() > oldLemmaList.size()) {
            newLemmaList = gradationCollectLemmas.getNewLemmaList(page, wordsMap, lemmas);
            if (!newLemmaList.isEmpty()) lemmaRepository.saveAll(newLemmaList);
        }

        List<IndexEntity> indexEntities = gradationCollectLemmas.getIndexList(page, wordsMap);
        if (!indexEntities.isEmpty()) indexRepository.saveAll(indexEntities);
    }

    private void incrementFrequencyAllLemmasEntity(Collection<LemmaEntity> list) {
        if (!list.isEmpty()) {
            list.forEach(lemmaEntity ->
                    lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1)
            );
            lemmaRepository.saveAll(list);
        }
    }
}
