package searchengine.services.sitemaps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.sites.Site;
import searchengine.config.sites.SitesList;
import searchengine.dto.enums.MessageType;
import searchengine.dto.indexing.IndexDto;
import searchengine.dto.sitemaps.ProcessorRun;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import searchengine.utils.sitemaps.SiteMap;
import searchengine.utils.sitemaps.TaskService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesList sites;
    private final TaskService taskService;
    private final SiteRepository siteRepository;

    private static final Set<ProcessorRun> siteThreadSet = new HashSet<>(0);
    private static final ThreadPoolExecutor fixedThreadPool =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(numberOfProcessorCores());

    @Override
    public IndexDto getIndexDtoAndStartIndexing() {
        log.info("1. IndexingService getStartIndexingResponse" +
                " siteService.getCountSites() - {}", siteRepository.countAllSites());
        IndexDto indexDto = new IndexDto();
        if (siteRepository.countAllByStatus(Status.INDEXING) != 0) {
            indexDto.setResult(false);
            indexDto.setError(MessageType.START_ERROR_MESSAGE.getDescription());
        } else {
            indexDto.setResult(true);
            taskStart();
        }
        log.info("2. IndexingService getStartIndexingResponse" +
                " indexDto {}", indexDto);
        return indexDto;
    }

    @Override
    public IndexDto getIndexDtoAndStopIndexing() {
        log.info("1. IndexingService getStopIndexingResponse");
        IndexDto indexDto = new IndexDto();
        if (siteRepository.countAllByStatus(Status.INDEXING) == 0) {
            indexDto.setResult(false);
            indexDto.setError(MessageType.STOP_ERROR_MESSAGE.getDescription());
        } else {
            taskStop();
            indexDto.setResult(true);
        }
        log.info("2. IndexingService getStopIndexingResponse" +
                " indexDto {}", indexDto);
        return indexDto;
    }

    private void taskStart() {
        clearSiteThreadSet();
        SiteMap.clearLinksSet();

        for (Site site : sites.getSites()) {
            ProcessorRun processorRun = taskService.startProcessorRun(site);
            siteThreadSet.add(processorRun);
            fixedThreadPool.execute(processorRun);
        }
    }

    private void taskStop() {
        siteThreadSet.forEach((processorRun) -> {
            fixedThreadPool.remove(processorRun);
            Thread task = taskService.stopProcessorRun(processorRun);
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

    private static int numberOfProcessorCores() {
        return Runtime.getRuntime().availableProcessors();
    }
}
