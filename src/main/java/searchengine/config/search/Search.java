package searchengine.config.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.CollectLemmas;
import searchengine.model.SiteEntity;
import searchengine.utils.gradations.IndexService;
import searchengine.utils.gradations.LemmaService;
import searchengine.utils.sitemaps.PageService;

@Component
public class Search {
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final CollectLemmas collectLemmas;
    private final Fragment fragment;

    @Autowired
    public Search(
            PageService pageService,
            LemmaService lemmaService,
            IndexService indexService,
            CollectLemmas collectLemmas,
            Fragment fragment) {
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.collectLemmas = collectLemmas;
        this.fragment = fragment;
    }

    /**
     * Метод осуществляет поиск страниц по переданному поисковому запросу (параметр query)
     * по сайту (параметр siteEntity) и создает список объектов класса Item
     *
     * @param query поисковый запрос
     * @param siteEntity  сайт, по которому осуществлять поиск
     * @return объект класса Scanning implements Callable<List<Item>> для выполнения задач параллельно
     */
    public Scanning startScanning(String query, SiteEntity siteEntity) {
        return new Scanning(query, siteEntity,
                pageService,
                lemmaService,
                indexService,
                collectLemmas,
                fragment);
    }
}
