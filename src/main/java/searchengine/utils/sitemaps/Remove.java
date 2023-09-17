package searchengine.utils.sitemaps;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.utils.gradations.GradationCollectLemmas;
import searchengine.utils.methods.Methods;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Remove {
    private final Methods methods;
    private final GradationCollectLemmas gradationCollectLemmas;

    public void deleteAll(Site site) {
        SiteEntity siteEntity = methods.getSiteEntity(site);
        if (siteEntity != null) {
            methods.deleteAllIndex(methods.getListPageIdBySiteEntity(siteEntity));
            methods.deleteLemmas(siteEntity);

            methods.deletePages(siteEntity);
            methods.deleteSite(siteEntity);
        }
    }

    public void deleteAllPage(Page page) {
        PageEntity pageEntity = methods.getPageById(page.getPageId());
        if (pageEntity != null) {
            methods.deleteAllIndexByPageId(pageEntity.getId());

            Map<String, Integer> wordsMap = gradationCollectLemmas.getLemmasMap(page);
            Collection<String> words = wordsMap.keySet();
            List<LemmaEntity> oldLemmaList = gradationCollectLemmas.getOldLemmaList(pageEntity.getSiteId(), words);

            methods.deleteLemmasByPage(page, getList(oldLemmaList), getCollection(oldLemmaList));
            methods.deletePage(pageEntity.getId());
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
