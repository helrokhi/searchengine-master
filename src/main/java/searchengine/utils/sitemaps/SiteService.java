package searchengine.utils.sitemaps;

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
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class SiteService {
    @Autowired
    private SiteRepository siteRepository;

    public List<SiteEntity> findAll() {
        return siteRepository.findAll();
    }

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

    public SiteEntity getSiteEntityByLink(String link, List<SiteEntity> siteEntityList) {
        for (SiteEntity siteEntity : siteEntityList) {
            if (link.startsWith(siteEntity.getUrl()) || siteEntity.getUrl().equals(link)) {
                return siteEntity;
            }
        }
        return null;
    }

    public boolean isLinkInSiteEntityList(String link, List<SiteEntity> siteEntityList) {
        return (getSiteEntityByLink(link, siteEntityList) != null);
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
        newStatusTime(siteEntity);
    }

    public void setStopStatusFailed(SiteEntity siteEntity) {
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError("Индексация остановлена пользователем");
        newStatusTime(siteEntity);
    }

    public void deleteSite(SiteEntity siteEntity) {
        System.out.println("\tSiteService deleteSite" +
                " SiteEntity " + siteEntity.getUrl() +
                "");
        siteRepository.delete(siteEntity);
    }
}