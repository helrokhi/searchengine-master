package searchengine.config.sitemaps;

import lombok.Getter;
import org.jsoup.nodes.Document;
import searchengine.config.sites.Site;
import searchengine.dto.sites.PageResponse;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.utils.methods.Methods;
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
    private Methods methods;
    private Remove remove;
    private ForkJoinPool forkJoinPool;
    private ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    public Processor(
            Site site,
            SiteMapRecursive siteMapRecursive,
            SiteMap siteMap,
            JsoupConnect jsoupConnect,
            Methods methods, Remove remove,
            ForkJoinPool forkJoinPool
    ) {
        this.site = site;
        this.siteMapRecursive = siteMapRecursive;
        this.siteMap = siteMap;
        this.jsoupConnect = jsoupConnect;
        this.methods =methods;
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
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setName(siteName);

        if (document == null) {
            methods.setStatusFailed(siteEntity, pageResponse);
            return;
        }

        siteEntity.setStatus(Status.INDEXING);
        methods.saveSite(siteEntity);

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

        siteEntity = methods.getSiteEntity(site);

        if (!isInterrupted()) {
            siteEntity.setStatus(Status.INDEXED);
            siteEntity.setLastError("");
            methods.saveSite(siteEntity);

            System.out.println("Processor run End of scanning" +
                    " site " + siteUrl +
                    "");
        } else {
            getForkJoinPool().shutdownNow();
            if (siteEntity != null && siteEntity.getStatus().equals(Status.INDEXING)) {
                methods.setStopStatusFailed(siteEntity);
            }
            System.out.println("Processor run STOP of scanning" +
                    " site " + siteUrl +
                    "");
        }
    }
}
