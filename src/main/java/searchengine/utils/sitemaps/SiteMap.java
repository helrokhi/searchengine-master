package searchengine.utils.sitemaps;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.dto.gradations.GradationThread;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.gradations.GradationService;
import searchengine.config.sites.Site;
import searchengine.dto.sites.PageDto;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@Getter
@Setter
public class SiteMap {
    private JsoupConnect jsoupConnect;
    private SiteRepository siteRepository;
    private PageRepository pageRepository;
    private static final Set<String> linksSet = new HashSet<>(0);
    private GradationService gradationService;

    @Autowired
    public SiteMap(JsoupConnect jsoupConnect,
                   SiteRepository siteRepository, PageRepository pageRepository,
                   GradationService gradationService) {
        this.jsoupConnect = jsoupConnect;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.gradationService = gradationService;
    }

    public void addLinks(SitePage page) {
        String link = page.getLink();
        if (!linksSet.contains(link)) {
            synchronized (linksSet) {
                linksSet.add(link);
            }
        }
    }

    public PageEntity setPageEntity(SitePage page) {
        PageEntity pageEntity = new PageEntity();
        PageDto pageDto = jsoupConnect.setPageDto(page.getLink());
        Document document = pageDto.getDocument();

        pageEntity.setSiteId(page.getSiteId());
        pageEntity.setPath(page.getSuffix());
        pageEntity.setCode(pageDto.getCode());

        if (document == null) {
            SiteEntity siteEntity = siteRepository.findSiteByUrl(page.getSite().getUrl());
            updateLastError(siteEntity, pageDto);
            pageEntity.setContent("");

            log.info("\tset pageEntity: site_id - {} " +
                            " path - {} code - {} content - {} ",
                    pageEntity.getSiteId(), pageEntity.getPath(),
                    pageEntity.getCode(), pageEntity.getContent().length());
        } else {
            pageEntity.setContent(document.html());
            String text = document.getElementsByTag("title").text()
                    .concat(" ")
                    .concat(document.getElementsByTag("body").text());
            page.setText(text);
        }
        return pageEntity;
    }

    public void subPagesSet(SitePage page) {
        String link = page.getLink();
        Site site = page.getSite();
        int id = page.getSiteId();

        PageDto pageDto = jsoupConnect.setPageDto(link);
        Document document = pageDto.getDocument();

        if (document == null) {
            SiteEntity siteEntity = siteRepository.findSiteByUrl(site.getUrl());
            updateLastError(siteEntity, pageDto);
        } else {
            Elements elements = document.select("a[href]");
            for (Element element : elements) {
                String subLink = element.attr("abs:href");
                SitePage subPage = new SitePage(subLink, link, site);
                subPage.setSiteId(id);

                if (!linksSet.contains(subLink)) {
                    page.addSubPages(subPage);
                }
            }
            page.getSubPages().forEach(this::addLinks);
        }
    }

    public void savePage(SitePage page) {
        PageEntity pageEntity = setPageEntity(page);
        savePageEntity(pageEntity);
        page.setPageId(pageEntity.getId());
    }

    public static void clearLinksSet() {
        log.info("\tSiteMap clearLinksSet linksSet.size() {}", linksSet.size());
        linksSet.clear();
    }

    private void savePageEntity(PageEntity pageEntity) {
        pageRepository.save(pageEntity);
        SiteEntity siteEntity =
                siteRepository.findById(pageEntity.getSiteId()).orElse(null);
        if (siteEntity != null) {
            siteEntity.setStatusTime(LocalDateTime.now());
            siteRepository.save(siteEntity);
        }
    }

    private void updateLastError(SiteEntity siteEntity, PageDto pageDto) {
        siteEntity.setLastError(pageDto.getException().getMessage());
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
    }

    public GradationThread startGradation(SitePage page) {
        return gradationService.startGradation(page);
    }
}
