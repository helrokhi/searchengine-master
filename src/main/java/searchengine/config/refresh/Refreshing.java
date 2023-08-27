package searchengine.config.refresh;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.Gradation;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.config.sites.SitesList;
import searchengine.model.PageEntity;
import searchengine.services.sitemaps.PageService;
import searchengine.services.sitemaps.RemoveService;
import searchengine.services.sitemaps.SiteMapService;
import searchengine.services.sitemaps.SiteService;

@Component
public class Refreshing {
    private Page page;
    private SiteMapService siteMapService;
    private SiteService siteService;
    private PageService pageService;
    private RemoveService removeService;

    @Autowired
    public Refreshing(
            SiteMapService siteMapService,
            SiteService siteService,
            PageService pageService,
            RemoveService removeService
    ) {
        this.siteMapService = siteMapService;
        this.siteService = siteService;
        this.pageService = pageService;
        this.removeService = removeService;
    }

    public void refreshPageEntity(String link, SitesList sites) {
        System.out.println("1. Refreshing refreshPageEntity" +
                " link " + link +
                "");
        Site site = siteService.getSite(link, sites);
        PageEntity pageEntity = getPageEntity(link, site);
        String url = site.getUrl();

        page = new Page(link, url, site);

        if (pageEntity == null) {
            page.setSiteId(siteService.getSiteEntity(site).getId());
        } else {
            page.setSiteId(pageEntity.getSiteId());
            page.setPageId(pageEntity.getId());
            page.setText(Jsoup.clean(pageEntity.getContent(), new Safelist()));

            removeService.deleteAllPage(page);
        }
        siteMapService.savePage(page);
        startGradation();

        System.out.println("2. Refreshing refreshPageEntity" +
                " pageEntity " + pageEntity +
                "");
    }

    private PageEntity getPageEntity(String link, Site site) {
        return pageService.getPageByPath(getPath(link, site));
    }

    private String getPath(String link, Site site) {
        return link.replace(site.getUrl(), "");
    }

    private void startGradation() {
        Gradation gradation = siteMapService.startGradation(page);
        gradation.start();
        try {
            gradation.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isLinkInSites(String link, SitesList sites) {
        return  siteService.isLinkInSites(link, sites);
    }
}
