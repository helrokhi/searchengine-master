package searchengine.config.gradations;

import searchengine.config.sitemaps.Page;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.utils.gradations.IndexService;
import searchengine.utils.gradations.LemmaService;
import searchengine.utils.gradations.WordsService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Gradation extends Thread {
    private final Page page;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final WordsService wordsService;

    public Gradation(
            Page page,
            LemmaService lemmaService,
            IndexService indexService,
            WordsService wordsService) {
        this.page = page;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.wordsService = wordsService;
    }

    @Override
    public void run() {
        Map<String, Integer> wordsMap = wordsService.getLemmasMap(page);
        Collection<String> words = wordsMap.keySet();
        List<LemmaEntity> oldLemmaList = wordsService.getOldLemmaList(page.getSiteId(), words);
        Collection<String> lemmas = wordsService.getLemmas(page.getSiteId(), words);

        lemmaService.incrementFrequencyAllLemmasEntity(oldLemmaList);

        List<LemmaEntity> newLemmaList;
        if (words.size() > oldLemmaList.size()) {
            newLemmaList = wordsService.getNewLemmaList(page, wordsMap, lemmas);
            lemmaService.saveAllNewLemmas(newLemmaList);
        }

        List<IndexEntity> indexList = wordsService.getIndexList(page, wordsMap);
        indexService.saveAllIndexEntity(indexList);
    }
}
