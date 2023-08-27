package searchengine.services.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.model.LemmaEntity;
import searchengine.services.gradations.WordsService;
import searchengine.services.gradations.IndexService;
import searchengine.services.gradations.LemmaService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RemoveService {
    private final SiteService siteService;
    private final PageService pageService;
    private final LemmaService lemmaService;
    private final IndexService indexService;
    private final WordsService wordsService;

    @Autowired
    public RemoveService(
            SiteService siteService,
            PageService pageService,
            LemmaService lemmaService,
            IndexService indexService,
            WordsService wordsService) {
        this.siteService = siteService;
        this.pageService = pageService;
        this.lemmaService = lemmaService;
        this.indexService = indexService;
        this.wordsService = wordsService;
    }

    public void deleteAll(Site site) {
        if (siteService.getSiteEntity(site) != null) {
            SiteMapService.clearLinksSet();

            indexService.deleteAllIndex(pageService.getListPageIdBySite(site));
            lemmaService.deleteLemmas(site);

            pageService.deletePages(site);
            siteService.deleteSite(site);
        }
    }

    public void deleteAllPage(Page page) {
        if (pageService.getPageById(page.getPageId()) != null) {
            indexService.deleteAllIndexByPageId(page.getPageId());

            Map<String, Integer> wordsMap = wordsService.getLemmasMap(page);
            Collection<String> words = wordsMap.keySet();
            List<LemmaEntity> oldLemmaList = wordsService.getOldLemmaList(page.getSiteId(), words);

            lemmaService.deleteLemmasByPage(page, getList(oldLemmaList), getCollection(oldLemmaList));
            pageService.deletePage(page.getPageId());
        }
    }

    private Collection<LemmaEntity> getCollection(List<LemmaEntity> oldLemmaList) {
        return oldLemmaList.stream()
                .filter(lemmaEntity -> lemmaEntity.getFrequency() > 1)
                .collect(Collectors.toList());
    }

    private List<Integer> getList(List<LemmaEntity> oldLemmaList) {
        return oldLemmaList.stream()
                .filter(lemmaEntity -> lemmaEntity.getFrequency() == 1)
                .map((lemmaEntity) -> lemmaEntity.getId())
                .collect(Collectors.toList());
    }
}
