package searchengine.utils.refresh;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;
import searchengine.dto.gradations.Gradation;
import searchengine.utils.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.PageRepository;
import searchengine.utils.sitemaps.Remove;
import searchengine.utils.sitemaps.SiteMap;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Refreshing {
    private Page page;
    private final SiteMap siteMap;
    private final PageRepository pageRepository;
    private final Remove remove;

    public void refreshPageEntity(String link, List<SiteEntity> siteEntityList) {
        System.out.println("1. Refreshing refreshPageEntity" +
                " link " + link +
                "");
        SiteEntity siteEntity = getSiteEntityByLink(link, siteEntityList);
        PageEntity pageEntity = getPageEntity(link, siteEntity);
        String url = siteEntity.getUrl();
        Site site = new Site();
        site.setUrl(siteEntity.getUrl());
        site.setName(siteEntity.getName());

        page = new Page(link, url, site);

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

        System.out.println("2. Refreshing refreshPageEntity" +
                " pageEntity " + pageEntity +
                "");
    }

    private PageEntity getPageEntity(String link, SiteEntity siteEntity) {
        return pageRepository
                .findByPath(getPath(link, siteEntity));
    }

    private String getPath(String link, SiteEntity siteEntity) {
        return link.replace(siteEntity.getUrl(), "");
    }

    private void startGradation() {
        Gradation gradation = siteMap.startGradation(page);
        gradation.start();
        try {
            gradation.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLinkInSites(String link, List<SiteEntity> siteEntityList) {
        return isLinkInSiteEntityList(link, siteEntityList);
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
}
