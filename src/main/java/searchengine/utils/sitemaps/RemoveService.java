package searchengine.utils.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.utils.gradations.WordsService;
import searchengine.utils.gradations.IndexService;
import searchengine.utils.gradations.LemmaService;

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
        SiteEntity siteEntity = siteService.getSiteEntity(site);
        if (siteEntity != null) {
            SiteMapService.clearLinksSet();

            indexService.deleteAllIndex(pageService.getListPageIdBySiteEntity(siteEntity));
            lemmaService.deleteLemmas(siteEntity);

            pageService.deletePages(siteEntity);
            siteService.deleteSite(siteEntity);
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
                .map(LemmaEntity::getId)
                .collect(Collectors.toList());
    }
}
