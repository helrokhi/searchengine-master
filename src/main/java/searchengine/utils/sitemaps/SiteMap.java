package searchengine.utils.sitemaps;

import lombok.Getter;
import lombok.Setter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.Gradation;
import searchengine.utils.gradations.Gradations;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.dto.sites.PageResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.utils.methods.Methods;

import java.util.HashSet;
import java.util.Set;

@Component
@Getter
@Setter
public class SiteMap {
    private JsoupConnect jsoupConnect;
    private Methods methods;
    private static final Set<String> linksSet = new HashSet<>(0);
    private Gradations gradations;

    @Autowired
    public SiteMap(JsoupConnect jsoupConnect, Methods methods, Gradations gradations) {
        this.jsoupConnect = jsoupConnect;
        this.methods = methods;
        this.gradations = gradations;
    }

    public void addLinks(Page page) {
        String link = page.getLink();
        if (!linksSet.contains(link)) {
            synchronized (linksSet) {
                linksSet.add(link);
            }
        }
    }

    public PageEntity setPageEntity(Page page) {
        PageEntity pageEntity = new PageEntity();
        PageResponse pageResponse = jsoupConnect.getPageResponse(page.getLink());

        Document document = pageResponse.getDocument();

        pageEntity.setSiteId(page.getSiteId());
        pageEntity.setPath(page.getSuffix());
        pageEntity.setCode(pageResponse.getCode());

        if (document == null) {
            SiteEntity siteEntity = methods.getSiteEntity(page.getSite());
            methods.newLastError(siteEntity, pageResponse);
            pageEntity.setContent("");

            System.out.println(
                    "\tset pageEntity:" +
                            " site_id - " + pageEntity.getSiteId() +
                            " path - " + pageEntity.getPath() +
                            " code " + pageEntity.getCode() +
                            " content " + pageEntity.getContent().length() +
                            "");
        } else {
            pageEntity.setContent(document.html());
            String text = document.getElementsByTag("title").text()
                    .concat(" ")
                    .concat(document.getElementsByTag("body").text());
            page.setText(text);
        }
        return pageEntity;
    }

    public void subPagesSet(Page page) {
        String link = page.getLink();
        Site site = page.getSite();
        int id = page.getSiteId();

        PageResponse pageResponse = jsoupConnect.getPageResponse(link);
        Document document = pageResponse.getDocument();

        if (document == null) {
            SiteEntity siteEntity = methods.getSiteEntity(site);
            methods.newLastError(siteEntity, pageResponse);
        } else {
            Elements elements = document.select("a[href]");
            for (Element element : elements) {
                String subLink = element.attr("abs:href");
                Page subPage = new Page(subLink, link, site);
                subPage.setSiteId(id);

                if (!linksSet.contains(subLink)) {
                    page.addSubPages(subPage);
                }
            }
            page.getSubPages().forEach(this::addLinks);
        }
    }

    public void savePage(Page page) {
        PageEntity pageEntity = setPageEntity(page);
        methods.savePageEntity(pageEntity);
        page.setPageId(pageEntity.getId());
    }

    public static void clearLinksSet() {
        System.out.println("\tSiteMap clearLinksSet" +
                " linksSet.size() " + linksSet.size() +
                "");
        linksSet.clear();
    }

    public Gradation startGradation(Page page) {
        return gradations.startGradation(page);
    }
}
