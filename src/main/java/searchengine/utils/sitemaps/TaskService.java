package searchengine.utils.sitemaps;

import searchengine.config.sites.Site;
import searchengine.dto.sitemaps.ProcessorRun;

public interface TaskService {
    ProcessorRun startProcessorRun(Site site);
    Thread stopProcessorRun(ProcessorRun processorRun);
}
