package searchengine.utils.sitemaps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.dto.sitemaps.Interrupter;
import searchengine.dto.sitemaps.Processor;
import searchengine.config.sites.Site;
import searchengine.repositories.SiteRepository;

import java.util.concurrent.ForkJoinPool;

@Component
@RequiredArgsConstructor
public class Indexing {
    private final SiteMapRecursive siteMapRecursive;
    private final SiteMap siteMap;
    private final JsoupConnect jsoupConnect;
    private final SiteRepository siteRepository;
    private final Remove remove;

    public Processor startIndexing(Site site) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        return new Processor(site, siteMapRecursive,
                siteMap, jsoupConnect,
                siteRepository, remove,
                forkJoinPool);
    }

    public Thread stopIndexing(Processor processor) {
        return new Thread(new Interrupter(processor));
    }
}
