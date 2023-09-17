package searchengine.utils.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.sitemaps.Page;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.ThreadPoolExecutor;

@Component
public class SiteMapRecursive extends RecursiveAction {
    private Page page;
    private SiteMap siteMap;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor poolExecutor;

    @Autowired
    public SiteMapRecursive(
            Page page,
            SiteMap siteMap,
            ForkJoinPool forkJoinPool,
            ThreadPoolExecutor poolExecutor
    ) {
        this.page = page;
        this.siteMap = siteMap;
        this.forkJoinPool = forkJoinPool;
        this.poolExecutor = poolExecutor;
    }

    @Override
    protected void compute() {
        savePage();
        Set<Page> subPagesSet = getSubPagesSet();
        submissions();

        Set<SiteMapRecursive> subTasks = new HashSet<>();
        for (Page subPage : subPagesSet) {
            if (isRunning()) {
                SiteMapRecursive task = new SiteMapRecursive(subPage, siteMap, forkJoinPool, poolExecutor);
                task.fork();
                subTasks.add(task);
            }
        }
        subTasks.forEach(ForkJoinTask::join);
    }

    private Set<Page> getSubPagesSet() {
        siteMap.subPagesSet(page);
        return isRunning() ? page.getSubPages() : new HashSet<>(0);
    }

    private void savePage() {
        if (isRunning()) {
            int queued = poolExecutor.getQueue().size();
            try {
                Thread.sleep(150L * queued);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            siteMap.savePage(page);
        }
    }

    private boolean isRunning() {
        return !(forkJoinPool.isShutdown() || forkJoinPool.isTerminated());
    }

    private void submissions() {
        if (isRunning()) {
            poolExecutor.execute(siteMap.startGradation(page));
        } else {
            poolExecutor.shutdown();
        }
    }
}
