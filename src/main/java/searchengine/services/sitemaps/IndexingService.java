package searchengine.services.sitemaps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import searchengine.utils.sitemaps.Indexing;
import searchengine.dto.sitemaps.Processor;
import searchengine.config.sites.Site;
import searchengine.config.sites.SitesList;
import searchengine.dto.indexing.IndexResponse;
import searchengine.utils.sitemaps.SiteMap;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class IndexingService {
    private final SitesList sites;
    private final Indexing indexing;
    private final SiteRepository siteRepository;
    private static final Set<Processor> siteThreadSet = new HashSet<>(0);
    private final ThreadPoolExecutor fixedThreadPool =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfProcessorCores());

    public IndexResponse getStartIndexingResponse() {
        System.out.println("1. IndexingService getStartIndexingResponse" +
                " siteService.getCountSites() - " + siteRepository.countAllSites() +
                "");
        IndexResponse response = new IndexResponse();
        if (siteRepository.countAllByStatus(Status.INDEXING) != 0) {
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
        if (siteRepository.countAllByStatus(Status.INDEXING) == 0) {
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
