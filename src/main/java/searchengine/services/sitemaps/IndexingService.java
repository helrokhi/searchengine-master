package searchengine.services.sitemaps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.sitemaps.Indexing;
import searchengine.config.sitemaps.Processor;
import searchengine.config.sites.Site;
import searchengine.config.sites.SitesList;
import searchengine.dto.indexing.IndexResponse;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IndexingService {
    private final SitesList sites;
    private final Indexing indexing;
    private final SiteService siteService;
    private static final Set<Processor> siteThreadSet = new HashSet<>(0);
    private Processor thread;
    private static boolean isStopped = false;

    public IndexResponse getStartIndexingResponse() {
        System.out.println("1. IndexingService getStartIndexingResponse" +
                " siteService.getCountSites() - " + siteService.getCountSites() +
                "");
        IndexResponse response = new IndexResponse();
        if (siteService.isIndexing()) {
            response.setResult(false);
            response.setError("Индексация уже запущена");
        } else {
            isStopped = false;
            taskStart();
            response.setResult(true);
            System.out.println("1.2 IndexingService getStartIndexingResponse" +
                    " siteThreadSet " + (siteThreadSet.size()) +
                    "");
        }

        System.out.println("2. IndexingService getStartIndexingResponse" +
                " response " + response +
                "");
        return response;
    }

    public IndexResponse getStopIndexingResponse() {
        System.out.println("1. IndexingService getStopIndexingResponse");
        IndexResponse response = new IndexResponse();
        if (!siteService.isIndexing()) {
            response.setResult(false);
            response.setError("Индексация не запущена");
        } else {
            isStopped = true;
            taskStop();
            response.setResult(true);
        }
        System.out.println("2. IndexingService getStopIndexingResponse" +
                " response " + response +
                "");
        return response;
    }

    private void taskStart() {
        for (Site site : sites.getSites()) {
            if (!isStopped) {
                Processor processor = indexing.startIndexing(site);

                siteThreadSet.add(processor);
                thread = processor;

                processor.start();

                System.out.println("\tIndexingService taskStart" +
                        " site " + processor.getSite().getUrl() +
                        " \n\tthread: " + thread.getName() +
                        "");
                try {
                    processor.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        taskStartFinished();
        clearSiteThreadSet();
    }

    private void taskStartFinished() {
        siteThreadSet.forEach((processor) -> {
            try {
                processor.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("\tIndexingService taskStart Scan finished!" +
                    " site " + processor.getSite().getUrl() +
                    "");
        });
    }

    private void taskStop() {
        Thread task = indexing.stopIndexing(thread);
        task.start();

        try {
            task.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("IndexingService taskStop" +
                " site " + thread.getSite().getUrl() +
                " \n\tprocessor " + thread.getName() +
                " isAlive " + thread.isAlive() +
                "");
    }

    public static void clearSiteThreadSet() {
        siteThreadSet.clear();
    }
}
