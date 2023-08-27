package searchengine.config.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import searchengine.config.gradations.CollectLemmas;
import searchengine.services.sitemaps.PageService;

@Component
public class Fragment {
    private final PageService pageService;
    private final CollectLemmas collectLemmas;

    @Autowired
    public Fragment(PageService pageService, CollectLemmas collectLemmas) {
        this.pageService = pageService;
        this.collectLemmas = collectLemmas;
    }

    public Snippet startSnippet(Item item) {
        return new Snippet(item, pageService, collectLemmas);
    }
}
