package searchengine.services.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.sites.Site;
import searchengine.config.sites.SitesList;
import searchengine.dto.sites.PageResponse;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class SiteService {
    @Autowired
    private SiteRepository siteRepository;

    public void saveSite(SiteEntity siteEntity) {
            siteRepository.save(siteEntity);
    }

    public long getStatusTimeLong(SiteEntity siteEntity) {
        return siteEntity.getStatusTime().toEpochSecond(ZoneOffset.UTC);
    }

    public int getCountSites() {
        return siteRepository.countAllSites();
    }

    public boolean isIndexing() {
        return siteRepository.countAllByStatus(Status.INDEXING) != 0;
    }

    public boolean isFailed() {
        return siteRepository.countAllByStatus(Status.FAILED) != 0;
    }

    public SiteEntity getSiteById(int id) {
        return siteRepository.findById(id).orElse(null);
    }

    public SiteEntity getSiteByUrl(String url) {
        return siteRepository.findSiteByUrl(url);
    }

    public SiteEntity getSiteEntity(Site site) {
        return getSiteByUrl(site.getUrl());
    }

    public Site getSite(String link, SitesList sites) {
        for (Site site : sites.getSites()) {
            if (link.startsWith(site.getUrl()) || site.getUrl().equals(link)) {
                return site;
            }
        }
        return null;
    }

    public boolean isLinkInSites(String link, SitesList sites) {
        return (getSite(link, sites) != null);
    }

    public void newStatusTime(SiteEntity siteEntity) {
        siteEntity.setStatusTime(LocalDateTime.now());
        saveSite(siteEntity);
    }

    public void newLastError(SiteEntity siteEntity, PageResponse pageResponse) {
        siteEntity.setLastError(pageResponse.getException().getMessage());
        newStatusTime(siteEntity);
    }

    public void setStatusFailed(SiteEntity siteEntity, PageResponse pageResponse) {
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError(pageResponse
                .getException()
                .getMessage());
        siteEntity.setName("No name. Indexing failed.");
        newStatusTime(siteEntity);
    }

    public void setStopStatusFailed(SiteEntity siteEntity) {
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError("Индексация остановлена пользователем");
        newStatusTime(siteEntity);
    }

    public void deleteSite(Site site) {
        System.out.println("\tSiteService deleteSite" +
                " SiteEntity " + getSiteEntity(site).getUrl() +
                "");
        siteRepository.delete(getSiteEntity(site));
    }
}