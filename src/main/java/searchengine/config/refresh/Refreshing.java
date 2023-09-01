package searchengine.config.refresh;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.Gradation;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.services.sitemaps.PageService;
import searchengine.services.sitemaps.RemoveService;
import searchengine.services.sitemaps.SiteMapService;
import searchengine.services.sitemaps.SiteService;

import java.util.List;

/**
 * класс является частью компонентов приложения
 */
@Component
public class Refreshing {
    private Page page;
    private SiteMapService siteMapService;
    private SiteService siteService;
    private PageService pageService;
    private RemoveService removeService;
    //автоматически найдет бины SiteMapService, SiteService, PageService, RemoveService
    // и внедрит в них в Refreshing.
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
    /**
     * Метод обновляет существующую (создает новую) отдельную страницу, адрес
     * которой передан в параметре в таблице page базы данных,
     * а также обновляет (создает) на основании HTML-код переданной страницы набор лемм
     * и их количество в таблицах lemma и index базы данных.
     * @param link адрес страницы
     * @param siteEntityList список сайтов из конфигурационного файла
     */
    public void refreshPageEntity(String link, List<SiteEntity> siteEntityList) {
        System.out.println("1. Refreshing refreshPageEntity" +
                " link " + link +
                "");
        SiteEntity siteEntity = siteService.getSiteEntityByLink(link, siteEntityList);
        PageEntity pageEntity = getPageEntity(link, siteEntity);
        String url = siteEntity.getUrl();
        Site site = new Site();
        site.setUrl(siteEntity.getUrl());
        site.setName(siteEntity.getUrl());

        page = new Page(link, url, site);

        if (pageEntity == null) {
            page.setSiteId(siteEntity.getId());
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

    private PageEntity getPageEntity(String link, SiteEntity siteEntity) {
        return pageService.getPageByPath(getPath(link, siteEntity));
    }

    private String getPath(String link, SiteEntity siteEntity) {
        return link.replace(siteEntity.getUrl(), "");
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

    public boolean isLinkInSites(String link, List<SiteEntity> siteEntityList) {
        return  siteService.isLinkInSiteEntityList(link, siteEntityList);
    }
}
