package searchengine.dto.sitemaps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import searchengine.dto.enums.MessageType;
import searchengine.config.sites.Site;
import searchengine.dto.sites.PageDto;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;
import searchengine.utils.sitemaps.*;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@AllArgsConstructor
@Getter
public class ProcessorRun extends Thread implements Runnable {
    private Site site;
    private SiteMapRecursive siteMapRecursive;
    private SiteMap siteMap;
    private JsoupConnect jsoupConnect;
    private SiteRepository siteRepository;
    private RemoveService remove;
    private ForkJoinPool forkJoinPool;

    @Override
    public void run() {
        log.info("RUN site: {}", site.getUrl());

        remove.deleteAll(site);

        String siteUrl = site.getUrl();
        String siteName = site.getName();

        PageDto pageDto = jsoupConnect.setPageDto(siteUrl);
        Document document = pageDto.getDocument();

        SiteEntity siteEntity = new SiteEntity();

        siteEntity.setUrl(siteUrl);
        siteEntity.setName(siteName);

        if (document == null) {
            saveNewStatus(siteEntity, Status.FAILED, pageDto.getException().getMessage());
            return;
        }

        saveNewStatus(siteEntity, Status.INDEXING, null);

        SitePage page = new SitePage(siteUrl, siteUrl, site);
        page.setSiteId(siteEntity.getId());
        page.setSuffix("/");
        siteMap.addLinks(page);

        siteMapRecursive = new SiteMapRecursive(page,
                siteMap,
                forkJoinPool,
                getPoolExecutor());

        log.info("2. RUN page: url: {} site_id {} link {} prefix {} suffix {}",
                siteEntity.getUrl(), page.getSiteId(),
                page.getLink(), page.getPrefix(), page.getSuffix());

        forkJoinPool.invoke(siteMapRecursive);

        siteEntity = siteRepository.findSiteByUrl(siteUrl);

        if (!isInterrupted()) {
            saveNewStatus(siteEntity, Status.INDEXED, MessageType.ENTITY_MESSAGE.getDescription());
            log.info("End of scanning site {}", siteUrl);
        } else {
            forkJoinPool.shutdownNow();
            if (siteEntity != null && siteEntity.getStatus().equals(Status.INDEXING)) {
                saveNewStatus(siteEntity, Status.FAILED, MessageType.STOP_MESSAGE.getDescription());
            }
            log.info("STOP of scanning site {}", siteUrl);
        }
    }

    private void saveNewStatus(SiteEntity siteEntity, Status status, String lastError) {
        siteEntity.setStatus(status);
        siteEntity.setLastError(lastError);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }

    public ThreadPoolExecutor getPoolExecutor() {
        return (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    }

}
