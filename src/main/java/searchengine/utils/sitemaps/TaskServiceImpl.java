package searchengine.utils.sitemaps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.sitemaps.InterrupterRun;
import searchengine.dto.sitemaps.ProcessorRun;
import searchengine.config.sites.Site;
import searchengine.repositories.SiteRepository;

import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {
    private final SiteMapRecursive siteMapRecursive;
    private final SiteMap siteMap;
    private final JsoupConnect jsoupConnect;
    private final SiteRepository siteRepository;
    private final RemoveService removeService;

    @Override
    public ProcessorRun startProcessorRun(Site site) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        return new ProcessorRun(site, siteMapRecursive,
                siteMap, jsoupConnect, siteRepository, removeService,
                forkJoinPool);
    }

    @Override
    public Thread stopProcessorRun(ProcessorRun processorRun) {
        return new Thread(new InterrupterRun(processorRun));
    }
}
