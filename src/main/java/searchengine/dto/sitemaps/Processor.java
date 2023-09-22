package searchengine.dto.sitemaps;

import lombok.Getter;
import org.jsoup.nodes.Document;
import searchengine.utils.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.dto.sites.PageResponse;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import searchengine.utils.sitemaps.*;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

@Getter
public class Processor extends Thread implements Runnable {
    private Site site;
    private SiteMapRecursive siteMapRecursive;
    private SiteMap siteMap;
    private JsoupConnect jsoupConnect;
    private SiteRepository siteRepository;
    private Remove remove;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor poolExecutor =
            (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    private static final String STOP_MESSAGE = "Индексация остановлена пользователем";

    public Processor(
            Site site,
            SiteMapRecursive siteMapRecursive,
            SiteMap siteMap,
            JsoupConnect jsoupConnect,
            SiteRepository siteRepository, Remove remove,
            ForkJoinPool forkJoinPool
    ) {
        this.site = site;
        this.siteMapRecursive = siteMapRecursive;
        this.siteMap = siteMap;
        this.jsoupConnect = jsoupConnect;
        this.siteRepository = siteRepository;
        this.remove = remove;
        this.forkJoinPool = forkJoinPool;
    }

    @Override
    public void run() {
        System.out.println("Processor run" +
                " site: " + site.getUrl() +
                "");

        remove.deleteAll(site);

        String siteUrl = site.getUrl();
        String siteName = site.getName();

        PageResponse pageResponse = jsoupConnect.getPageResponse(siteUrl);
        Document document = pageResponse.getDocument();

        SiteEntity siteEntity = new SiteEntity();

        siteEntity.setUrl(siteUrl);
        siteEntity.setName(siteName);

        if (document == null) {
            setStatus(siteEntity, Status.FAILED, pageResponse.getException().getMessage());
            return;
        }

        setStatus(siteEntity, Status.INDEXING, null);

        Page page = new Page(siteUrl, siteUrl, site);
        page.setSiteId(siteEntity.getId());
        page.setSuffix("/");
        siteMap.addLinks(page);

        siteMapRecursive = new SiteMapRecursive(page, siteMap, forkJoinPool, poolExecutor);

        System.out.println(
                "2. Processor run page:" +
                        " url: " + siteEntity.getUrl() +
                        " site_id " + page.getSiteId() +
                        " link " + page.getLink() +
                        " prefix " + page.getPrefix() +
                        " suffix " + page.getSuffix() +
                        "");

        forkJoinPool.invoke(siteMapRecursive);

        siteEntity = siteRepository.findSiteByUrl(siteUrl);

        if (!isInterrupted()) {
            setStatus(siteEntity, Status.INDEXED,"");
            System.out.println("Processor run End of scanning" +
                    " site " + siteUrl +
                    "");
        } else {
            getForkJoinPool().shutdownNow();
            if (siteEntity != null && siteEntity.getStatus().equals(Status.INDEXING)) {
                setStatus(siteEntity, Status.FAILED, STOP_MESSAGE);
            }
            System.out.println("Processor run STOP of scanning" +
                    " site " + siteUrl +
                    "");
        }
    }

    private void setStatus(SiteEntity siteEntity, Status status, String lastError) {
        siteEntity.setStatus(status);
        siteEntity.setLastError(lastError);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }
}
