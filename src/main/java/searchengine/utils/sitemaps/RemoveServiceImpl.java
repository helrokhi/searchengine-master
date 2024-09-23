package searchengine.utils.sitemaps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import searchengine.config.sites.Site;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;
import searchengine.utils.gradations.GradationCollectLemmas;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveServiceImpl implements RemoveService {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final GradationCollectLemmas gradationCollectLemmas;

    @Override
    public void deleteAll(Site site) {
        SiteEntity siteEntity = siteRepository.findSiteByUrl(site.getUrl());
        if (siteEntity != null) {
            int siteId = siteEntity.getId();
            List<Integer> listPageIdBySiteEntity =
                    (List<Integer>) pageRepository.findAllPagesIdBySiteId(siteId);

            deleteAllIndex(listPageIdBySiteEntity);

            List<Integer> listLemmasIdBySiteId =
                    (List<Integer>) lemmaRepository.findAllLemmasIdBySiteId(siteId);

            lemmaRepository.deleteAllByIdInBatch(listLemmasIdBySiteId);
            log.info("\tLemmaService deleteLemmas siteEntity {}  count {} siteId {}",
                    siteEntity.getUrl(), listLemmasIdBySiteId.size(), siteId);

            pageRepository.deleteAllByIdInBatch(listPageIdBySiteEntity);
            log.info("\tPageService deletePages siteEntity: {} count: {} siteId: {}",
                    siteEntity.getUrl(), listPageIdBySiteEntity.size(), siteEntity.getId());
            log.info("\tSiteService deleteSite SiteEntity {}", siteEntity.getUrl());
            siteRepository.delete(siteEntity);
        }
    }

    @Override
    public void deleteAllPage(SitePage page) {
        PageEntity pageEntity = pageRepository
                .findById(page.getPageId()).orElse(null);
        if (pageEntity != null) {
            deleteAllIndexByPageId(pageEntity.getId());

            Map<String, Integer> wordsMap = gradationCollectLemmas.getLemmasMap(page);
            Collection<String> words = wordsMap.keySet();
            List<LemmaEntity> oldLemmaList =
                    lemmaRepository.findByLemmas(pageEntity.getSiteId(), words);

            deleteLemmasByPage(page,
                    getListOldLemmasWhoseFrequencyIsOne(oldLemmaList),
                    getCollectionOldLemmasWhoseFrequencyGreaterOne(oldLemmaList));
            log.info("\tPageService deletePage pageId {}", + pageEntity.getId());
            pageRepository.deleteById(pageEntity.getId());
        }
    }

    private Collection<LemmaEntity> getCollectionOldLemmasWhoseFrequencyGreaterOne(
            List<LemmaEntity> oldLemmaList) {
        return oldLemmaList.stream()
                .filter(lemmaEntity -> lemmaEntity.getFrequency() > 1)
                .collect(Collectors.toList());
    }

    private List<Integer> getListOldLemmasWhoseFrequencyIsOne(List<LemmaEntity> oldLemmaList) {
        return oldLemmaList.stream()
                .filter(lemmaEntity -> lemmaEntity.getFrequency() == 1)
                .map(LemmaEntity::getId)
                .collect(Collectors.toList());
    }

    private void deleteLemmasByPage(
            SitePage page,
            List<Integer> list,
            Collection<LemmaEntity> collection) {
        lemmaRepository.deleteAllByIdInBatch(list);
        decrementFrequencyAllLemmasEntity(collection);
        log.info("\tLemmaService deleteLemmasByPage page {}", page.getLink());
    }

    private void decrementFrequencyAllLemmasEntity(Collection<LemmaEntity> list) {
        if (!list.isEmpty()) {
            list.forEach(lemmaEntity ->
                    lemmaEntity.setFrequency(lemmaEntity.getFrequency() - 1)
            );
            lemmaRepository.saveAll(list);
        }
    }

    private void deleteAllIndex(List<Integer> listPageId) {
        log.info("\tIndexService deleteIndex count {}", + listPageId.size());
        listPageId.forEach(this::deleteAllIndexByPageId);
    }

    private void deleteAllIndexByPageId(int pageId) {
        List<Integer> list = (List<Integer>) indexRepository.findAllIndexIdByPageId(pageId);
        if (!list.isEmpty()) indexRepository.deleteAllByIdInBatch(list);
    }
}
