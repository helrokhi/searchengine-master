package searchengine.dto.sitemaps;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class InterrupterRun implements Runnable {
    private final ProcessorRun processorRun;

    @Override
    public void run() {
        processorRun.interrupt();
        processorRun.getForkJoinPool().shutdown();
        processorRun.getPoolExecutor().getQueue().clear();
        log.info("interrupt run site: {} stop! queued {}",
                processorRun.getSite().getUrl(),
                processorRun.getPoolExecutor().getQueue().size());
    }
}
