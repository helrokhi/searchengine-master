package searchengine.services.sitemaps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.utils.sitemaps.Indexing;
import searchengine.config.sitemaps.Processor;
import searchengine.config.sites.Site;
import searchengine.config.sites.SitesList;
import searchengine.dto.indexing.IndexResponse;
import searchengine.utils.methods.Methods;
import searchengine.utils.sitemaps.SiteMap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingService {
    private final SitesList sites;
    private final Indexing indexing;
    private final Methods methods;
    private static final Set<Processor> siteThreadSet = new HashSet<>(0);
    private final ThreadPoolExecutor fixedThreadPool =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfProcessorCores());

    public IndexResponse getStartIndexingResponse() {
        System.out.println("1. IndexingService getStartIndexingResponse" +
                " siteService.getCountSites() - " + methods.getCountSites() +
                "");
        IndexResponse response = new IndexResponse();
        if (methods.isIndexing()) {
            response.setResult(false);
            response.setError("Индексация уже запущена.");
        } else {
            response.setResult(true);
            taskStart();
        }
        System.out.println("2. IndexingService getStartIndexingResponse" +
                " response " + response +
                "");
        return response;
    }

    public IndexResponse getStopIndexingResponse() {
        System.out.println("1. IndexingService getStopIndexingResponse");
        IndexResponse response = new IndexResponse();
        if (!methods.isIndexing()) {
            response.setResult(false);
            response.setError("Индексация не запущена");
        } else {
            taskStop();
            response.setResult(true);
        }
        System.out.println("2. IndexingService getStopIndexingResponse" +
                " response " + response +
                "");
        return response;
    }

    private void taskStart() {
        clearSiteThreadSet();
        SiteMap.clearLinksSet();

        for (Site site : sites.getSites()) {
            Processor processor = indexing.startIndexing(site);
            siteThreadSet.add(processor);
            fixedThreadPool.execute(processor);
        }
    }

    private void taskStop() {
        siteThreadSet.forEach((processor) -> {
            fixedThreadPool.remove(processor);
            Thread task = indexing.stopIndexing(processor);
            task.start();

            try {
                task.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void clearSiteThreadSet() {
        siteThreadSet.clear();
    }

    private int numberOfProcessorCores() {
        return Runtime.getRuntime().availableProcessors();
    }
}
