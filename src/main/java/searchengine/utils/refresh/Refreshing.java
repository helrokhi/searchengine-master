package searchengine.utils.refresh;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Service;
import searchengine.dto.gradations.GradationThread;
import searchengine.config.sites.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.utils.sitemaps.RemoveService;
import searchengine.utils.sitemaps.SiteMap;
import searchengine.utils.sitemaps.SitePage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class Refreshing implements RefreshService{
    private SitePage page;
    private final SiteMap siteMap;
    private final PageRepository pageRepository;
    private final RemoveService remove;

    public void refreshPageEntity(String link, List<SiteEntity> siteEntityList) {
        log.info("1.Refresh PageEntity link {}", link);
        SiteEntity siteEntity = getSiteEntityByLink(link, siteEntityList);
        PageEntity pageEntity = getPageEntity(link, siteEntity);
        String url = siteEntity.getUrl();
        Site site = new Site();
        site.setUrl(siteEntity.getUrl());
        site.setName(siteEntity.getName());

        page = new SitePage(link, url, site);

        if (pageEntity == null) {
            page.setSiteId(siteEntity.getId());
        } else {
            page.setSiteId(pageEntity.getSiteId());
            page.setPageId(pageEntity.getId());
            page.setText(Jsoup.clean(pageEntity.getContent(), new Safelist()));

            remove.deleteAllPage(page);
        }
        siteMap.savePage(page);
        startGradation();

        log.info("2. Refresh PageEntity pageEntity {}", pageEntity);
    }

    private PageEntity getPageEntity(String link, SiteEntity siteEntity) {
        return pageRepository
                .findByPath(getPath(link, siteEntity));
    }

    private String getPath(String link, SiteEntity siteEntity) {
        return link.replace(siteEntity.getUrl(), "");
    }

    private void startGradation() {
        GradationThread gradationThread = siteMap.startGradation(page);
        gradationThread.start();
        try {
            gradationThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLinkInSites(String link, List<SiteEntity> siteEntityList) {
        return isLinkInSiteEntityList(link, siteEntityList);
    }

    private SiteEntity getSiteEntityByLink(String link, List<SiteEntity> siteEntityList) {
        for (SiteEntity siteEntity : siteEntityList) {
            if (link.startsWith(siteEntity.getUrl()) || siteEntity.getUrl().equals(link)) {
                return siteEntity;
            }
        }
        return null;
    }

    private boolean isLinkInSiteEntityList(String link, List<SiteEntity> siteEntityList) {
        return (getSiteEntityByLink(link, siteEntityList) != null);
    }
}
