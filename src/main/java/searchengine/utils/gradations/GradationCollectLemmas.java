package searchengine.utils.gradations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.sitemaps.Page;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.repositories.LemmaRepository;

import java.util.*;

@Component
@RequiredArgsConstructor
@Getter
@Setter
public class GradationCollectLemmas {
    private final CollectLemmas collectLemmas;
    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;

    private Map<String, Integer> getCollectLemmas(String text) {
        return collectLemmas.collectLemmas(text);
    }

    public Map<String, Integer> getLemmasMap(Page page) {
        return getCollectLemmas(page.getText());
    }

    public List<LemmaEntity> getNewLemmaList(
            Page page,
            Map<String, Integer> wordsMap,
            Collection<String> lemmas) {
        List<LemmaEntity> newLemmaList = new ArrayList<>(0);
        wordsMap.forEach((key, value) -> {
            if (!lemmas.contains(key)) {
                LemmaEntity lemmaEntity = new LemmaEntity();
                lemmaEntity.setSiteId(page.getSiteId());
                lemmaEntity.setLemma(key);
                lemmaEntity.setFrequency(1);
                newLemmaList.add(lemmaEntity);
            }
        });
        return newLemmaList;
    }

    public List<IndexEntity> getIndexList(Page page, Map<String, Integer> wordsMap) {
        List<IndexEntity> list = new ArrayList<>(0);
        wordsMap.forEach((key, value) -> {
            int lemmaId = lemmaRepository.getLemmaId(key, page.getSiteId());
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setPageId(page.getPageId());
            indexEntity.setLemmaId(lemmaId);
            indexEntity.setRank(value);
            list.add(indexEntity);
        });
        return list;
    }
}
