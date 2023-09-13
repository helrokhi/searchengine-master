package searchengine.config.sitemaps;

import lombok.Getter;
import org.jsoup.nodes.Document;
import searchengine.config.sites.Site;
import searchengine.dto.sites.PageResponse;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.utils.sitemaps.*;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

@Getter
public class Processor extends Thread implements Runnable {
    private Site site;
    private SiteMap siteMap;
    private SiteMapService siteMapService;
    private ConnectService connectService;
    private SiteService siteService;
    private PageService pageService;
    private RemoveService removeService;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public Processor(
            Site site,
            SiteMap siteMap,
            SiteMapService siteMapService,
            ConnectService connectService,
            SiteService siteService,
            PageService pageService,
            RemoveService removeService,
            ForkJoinPool forkJoinPool
    ) {
        this.site = site;
        this.siteMap = siteMap;
        this.siteMapService = siteMapService;
        this.connectService = connectService;
        this.siteService = siteService;
        this.pageService = pageService;
        this.removeService = removeService;
        this.forkJoinPool = forkJoinPool;
    }

    @Override
    public void run() {
        System.out.println("Processor run" +
                " site: " + site.getUrl() +
                "");

        removeService.deleteAll(site);

        String siteUrl = site.getUrl();
        String siteName = site.getName();

        PageResponse pageResponse = connectService.getPageResponse(siteUrl);
        Document document = pageResponse.getDocument();

        SiteEntity siteEntity = new SiteEntity();

        siteEntity.setUrl(siteUrl);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setName(siteName);

        if (document == null) {
            siteService.setStatusFailed(siteEntity, pageResponse);
            return;
        }

        siteEntity.setStatus(Status.INDEXING);
        siteService.saveSite(siteEntity);

        Page page = new Page(siteUrl, siteUrl, site);
        page.setSiteId(siteEntity.getId());
        page.setSuffix("/");
        siteMapService.addLinks(page);

        siteMap = new SiteMap(page, siteMapService, forkJoinPool, poolExecutor);

        System.out.println(
                "2. Processor run page:" +
                        " url: " + siteEntity.getUrl() +
                        " site_id " + page.getSiteId() +
                        " link " + page.getLink() +
                        " prefix " + page.getPrefix() +
                        " suffix " + page.getSuffix() +
                        "");

        forkJoinPool.invoke(siteMap);

        siteEntity = siteService.getSiteEntity(site);

        if (!isInterrupted()) {
            siteEntity.setStatus(Status.INDEXED);
            siteEntity.setLastError(null);
            siteService.saveSite(siteEntity);

            System.out.println("Processor run End of scanning" +
                    " site " + siteUrl +
                    "");
        } else {
            getForkJoinPool().shutdownNow();
            if (siteEntity != null && siteEntity.getStatus().equals(Status.INDEXING)) {
                siteService.setStopStatusFailed(siteEntity);
            }
            System.out.println("Processor run STOP of scanning" +
                    " site " + siteUrl +
                    "");
        }
    }
}
