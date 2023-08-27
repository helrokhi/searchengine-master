package searchengine.config.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.sites.Site;
import searchengine.services.sitemaps.*;

import java.util.concurrent.ForkJoinPool;

@Component
public class Indexing {
    private final SiteMap siteMap;
    private final SiteMapService siteMapService;
    private final ConnectService connectService;
    private final SiteService siteService;
    private final PageService pageService;
    private final RemoveService removeService;

    @Autowired
    public Indexing(
            SiteMap siteMap,
            SiteMapService siteMapService,
            ConnectService connectService,
            SiteService siteService,
            PageService pageService,
            RemoveService removeService
    ) {
        this.siteMap = siteMap;
        this.siteMapService = siteMapService;
        this.connectService = connectService;

        this.siteService = siteService;
        this.pageService = pageService;

        this.removeService = removeService;
    }

    public Processor startIndexing(Site site) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        return new Processor(site, siteMap,
                siteMapService, connectService,
                siteService, pageService,
                removeService,
                forkJoinPool);
    }

    public Thread stopIndexing(Processor processor) {
        return new Thread(new Interrupter(processor));
    }
}
