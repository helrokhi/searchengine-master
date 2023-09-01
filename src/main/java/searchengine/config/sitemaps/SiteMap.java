package searchengine.config.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.services.sitemaps.SiteMapService;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class SiteMap extends RecursiveAction {
    private Page page;
    private SiteMapService siteMapService;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor poolExecutor;

    @Autowired
    public SiteMap(
            Page page,
            SiteMapService siteMapService,
            ForkJoinPool forkJoinPool,
            ThreadPoolExecutor poolExecutor
    ) {
        this.page = page;
        this.siteMapService = siteMapService;
        this.forkJoinPool = forkJoinPool;
        this.poolExecutor = poolExecutor;
    }

    @Override
    protected void compute() {
        savePage();
        Set<Page> subPagesSet = getSubPagesSet();
        submissions();

        Set<SiteMap> subTasks = new HashSet<>();
        for (Page subPage : subPagesSet) {
            if (isRunning()) {
                SiteMap task = new SiteMap(subPage, siteMapService, forkJoinPool, poolExecutor);
                task.fork();
                subTasks.add(task);
            }
        }
        subTasks.forEach(ForkJoinTask::join);
    }

    private Set<Page> getSubPagesSet() {
        siteMapService.subPagesSet(page);
        return isRunning() ? page.getSubPages() : new HashSet<>(0);
    }

    private void savePage() {
        if (isRunning()) {
            int queued = poolExecutor.getQueue().size();
            try {
                Thread.sleep(100 * queued);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            siteMapService.savePage(page);
        }
    }

    private boolean isRunning() {
        return !(forkJoinPool.isShutdown() || forkJoinPool.isTerminated());
    }

    private void submissions() {
        if (isRunning()) {
            poolExecutor.execute(siteMapService.startGradation(page));
        } else {
            poolExecutor.shutdown();
        }
    }
}
