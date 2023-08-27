package searchengine.config.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.CollectLemmas;
import searchengine.config.sites.Site;
import searchengine.services.gradations.IndexService;
import searchengine.services.gradations.LemmaService;
import searchengine.services.sitemaps.PageService;
import searchengine.services.sitemaps.SiteService;

@Component
public class Search {
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final CollectLemmas collectLemmas;
    private final Fragment fragment;

    @Autowired
    public Search(
            SiteService siteService,
            PageService pageService,
            LemmaService lemmaService,
            IndexService indexService,
            CollectLemmas collectLemmas,
            Fragment fragment) {
        this.siteService = siteService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.collectLemmas = collectLemmas;
        this.fragment = fragment;
    }

    public Scanning startScanning(String query, Site site) {
        return new Scanning(query, site,
                siteService,
                pageService,
                lemmaService,
                indexService,
                collectLemmas,
                fragment);
    }
}
