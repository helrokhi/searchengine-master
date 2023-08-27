package searchengine.config.sitemaps;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import searchengine.config.sites.Site;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Component
@Getter
@Setter
@NoArgsConstructor
public class Page {
    private String link;
    private String prefix;
    private Site site;
    private Set<Page> subPages;
    private String suffix;
    private String text;

    private int siteId;
    private int pageId;

    public Page(String link, String prefix, Site site) {
        this.link = link;
        this.prefix = prefix;
        this.subPages = new HashSet<>(0);
        this.site = site;
        this.suffix = link.replace(site.getUrl(), "");
        this.text = "";
    }

    public void addSubPages(Page subPage) {
        if (isLink(subPage.getLink()) && !subPages.contains(subPage)) {
            synchronized (subPages) {
                subPages.add(subPage);
            }
        }
    }

    private boolean isLink(String link) {
        return (!link.isEmpty() &&
                link.startsWith(prefix) &&
                (link.endsWith("/") || link.endsWith(".html")) &&
                !link.contains("#") &&
                //link.contains("courses") &&
                //!link.matches("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)") &&
                !link.contains(" "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page page = (Page) o;
        return link.equals(page.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
