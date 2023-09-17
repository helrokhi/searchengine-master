package searchengine.utils.sitemaps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.sitemaps.Interrupter;
import searchengine.config.sitemaps.Processor;
import searchengine.config.sites.Site;
import searchengine.utils.methods.Methods;

import java.util.concurrent.ForkJoinPool;

@Component
@RequiredArgsConstructor
public class Indexing {
    private final SiteMapRecursive siteMapRecursive;
    private final SiteMap siteMap;
    private final JsoupConnect jsoupConnect;
    private final Methods methods;
    private final Remove remove;

    public Processor startIndexing(Site site) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        return new Processor(site, siteMapRecursive,
                siteMap, jsoupConnect,
                methods, remove,
                forkJoinPool);
    }

    public Thread stopIndexing(Processor processor) {
        return new Thread(new Interrupter(processor));
    }
}
