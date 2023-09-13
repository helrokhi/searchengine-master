package searchengine.utils.gradations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.gradations.CollectLemmas;
import searchengine.config.sitemaps.Page;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;

import java.util.*;

@Service
@Getter
@Setter
public class WordsService {
    private final CollectLemmas collectLemmas;
    private final LemmaService lemmaService;
    private final IndexService indexService;

    @Autowired
    public WordsService(
            CollectLemmas collectLemmas,
            LemmaService lemmaService,
            IndexService indexService
    ) {
        this.collectLemmas = collectLemmas;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
    }

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
            int lemmaId = lemmaService.findLemmaIdByLemmaAndSiteId(key, page.getSiteId());
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setPageId(page.getPageId());
            indexEntity.setLemmaId(lemmaId);
            indexEntity.setRank(value);
            list.add(indexEntity);
        });
        return list;
    }

    public List<LemmaEntity> getOldLemmaList(int siteId, Collection<String> words) {
        return lemmaService.getAllLemmasEntity(siteId, words);
    }

    public Collection<String> getLemmas(int siteId, Collection<String> words) {
        return lemmaService.getAllLemmas(siteId, words);
    }
}
