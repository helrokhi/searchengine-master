package searchengine.utils.methods;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.dto.sites.PageResponse;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageRepository;
import searchengine.repositories.SiteRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Methods {
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    public List<SiteEntity> findAllSiteEntities() {
        return siteRepository.findAll();
    }

    public void saveSite(SiteEntity siteEntity) {
        siteRepository.save(siteEntity);
    }

    public int getCountSites() {
        return siteRepository.countAllSites();
    }

    public boolean isIndexing() {
        return siteRepository.countAllByStatus(Status.INDEXING) != 0;
    }

    public boolean isFailed() {
        return siteRepository.countAllByStatus(Status.FAILED) != 0;
    }

    public SiteEntity getSiteById(int id) {
        return siteRepository.findById(id).orElse(null);
    }

    public SiteEntity getSiteByUrl(String url) {
        return siteRepository.findSiteByUrl(url);
    }

    public SiteEntity getSiteEntity(Site site) {
        return getSiteByUrl(site.getUrl());
    }

    public SiteEntity getSiteEntityByLink(String link, List<SiteEntity> siteEntityList) {
        for (SiteEntity siteEntity : siteEntityList) {
            if (link.startsWith(siteEntity.getUrl()) || siteEntity.getUrl().equals(link)) {
                return siteEntity;
            }
        }
        return null;
    }

    public boolean isLinkInSiteEntityList(String link, List<SiteEntity> siteEntityList) {
        return (getSiteEntityByLink(link, siteEntityList) != null);
    }

    public void newStatusTime(SiteEntity siteEntity) {
        siteEntity.setStatusTime(LocalDateTime.now());
        saveSite(siteEntity);
    }

    public void newLastError(SiteEntity siteEntity, PageResponse pageResponse) {
        siteEntity.setLastError(pageResponse.getException().getMessage());
        newStatusTime(siteEntity);
    }

    public void setStatusFailed(SiteEntity siteEntity, PageResponse pageResponse) {
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError(pageResponse
                .getException()
                .getMessage());
        newStatusTime(siteEntity);
    }

    public void setStopStatusFailed(SiteEntity siteEntity) {
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError("Индексация остановлена пользователем");
        newStatusTime(siteEntity);
    }

    public void deleteSite(SiteEntity siteEntity) {
        System.out.println("\tSiteService deleteSite" +
                " SiteEntity " + siteEntity.getUrl() +
                "");
        siteRepository.delete(siteEntity);
    }

    public PageEntity getPageById(Integer pageId) {
        return pageRepository
                .findById(pageId).orElse(null);
    }

    public PageEntity getPageByPath(String path) {
        return pageRepository
                .findByPath(path);
    }

    public int getCountPages() {
        return pageRepository.countAllPages();
    }

    public int getCountAllPagesBySiteEntity(SiteEntity siteEntity) {
        return pageRepository
                .countAllPagesBySiteId(siteEntity.getId());
    }

    public void savePageEntity(PageEntity pageEntity) {
        pageRepository.save(pageEntity);
        newStatusTime(getSiteById(pageEntity.getSiteId()));
    }

    public List<Integer> getListPageIdBySiteEntity(SiteEntity siteEntity) {
        return (List<Integer>) pageRepository.findAllPagesIdBySiteId(siteEntity.getId());
    }

    public void deletePages(SiteEntity siteEntity) {
        System.out.println("\tPageService deletePages" +
                " siteEntity: " + siteEntity.getUrl() +
                " count: " + getListPageIdBySiteEntity(siteEntity).size() +
                " siteId: " + siteEntity.getId() +
                "");
        pageRepository.deleteAllByIdInBatch(getListPageIdBySiteEntity(siteEntity));
    }

    public void deletePage(int pageId) {
        System.out.print("\tPageService deletePage" +
                " pageId " + pageId +
                "");
        pageRepository.deleteById(pageId);
    }

    public void saveAllNewLemmas(List<LemmaEntity> newLemmaList) {
        if (!newLemmaList.isEmpty()) lemmaRepository.saveAll(newLemmaList);
    }

    public LemmaEntity findLemmaByLemmaAndSiteId(String key, SiteEntity siteEntity) {
        return lemmaRepository.findByLemma(key, siteEntity.getId());
    }

    public Integer findLemmaIdByLemmaAndSiteId(String key, int siteId) {
        return lemmaRepository.getLemmaId(key, siteId);
    }

    public int getCountLemmas() {
        return lemmaRepository.countAllLemmas();
    }

    public int getCountLemmasBySiteEntity(SiteEntity siteEntity) {
        return lemmaRepository.countAllLemmasBySiteId(siteEntity.getId());
    }

    public List<LemmaEntity> getAllLemmasEntity(int siteId, Collection<String> words) {
        return lemmaRepository.findByLemmas(siteId, words);
    }

    public Collection<String> getAllLemmas(int siteId, Collection<String> words) {
        return lemmaRepository.findAllLemmaInLemma(siteId, words);
    }

    public void incrementFrequencyAllLemmasEntity(Collection<LemmaEntity> list) {
        if (!list.isEmpty()) {
            list.forEach(lemmaEntity ->
                    lemmaEntity.setFrequency(lemmaEntity.getFrequency() + 1)
            );
            lemmaRepository.saveAll(list);
        }
    }

    public void decrementFrequencyAllLemmasEntity(Collection<LemmaEntity> list) {
        if (!list.isEmpty()) {
            list.forEach(lemmaEntity ->
                    lemmaEntity.setFrequency(lemmaEntity.getFrequency() - 1)
            );
            lemmaRepository.saveAll(list);
        }
    }

    public void deleteLemmas(SiteEntity siteEntity) {
        int siteId = siteEntity.getId();
        List<Integer> list = (List<Integer>) lemmaRepository.findAllLemmasIdBySiteId(siteId);

        lemmaRepository.deleteAllByIdInBatch(list);
        System.out.println("\tLemmaService deleteLemmas" +
                " siteEntity " + siteEntity.getUrl() +
                " count " + list.size() +
                " siteId " + siteId +
                "");
    }

    public void deleteLemmasByPage(
            Page page,
            List<Integer> list,
            Collection<LemmaEntity> collection
    ) {
        lemmaRepository.deleteAllByIdInBatch(list);
        decrementFrequencyAllLemmasEntity(collection);
        System.out.println("\tLemmaService deleteLemmasByPage" +
                " page " + page.getLink() +
                "");
    }

    public List<Integer> getLemmaIdListByLemmaEntityList(List<LemmaEntity> list) {
        return list.stream()
                .map(LemmaEntity::getId)
                .collect(Collectors.toList());
    }

    public void saveAllIndexEntity(List<IndexEntity> indexEntities) {
        if (!indexEntities.isEmpty()) {
            indexRepository.saveAll(indexEntities);
        }
    }

    public List<Integer> getListIndexId(int pageId) {
        return (List<Integer>) indexRepository.findAllIndexIdByPageId(pageId);
    }

    public void deleteAllIndex(List<Integer> listPageId) {
        System.out.println("\tIndexService deleteIndex" +
                " count " + listPageId.size() +
                "");
        listPageId.forEach(this::deleteAllIndexByPageId);
    }

    public void deleteAllIndexByPageId(int pageId) {
        List<Integer> list = getListIndexId(pageId);
        if (!list.isEmpty()) indexRepository.deleteAllByIdInBatch(list);
    }

    public List<Integer> getAllPageIdByLemmaId(int lemmaId, List<Integer> listPageIdInSite) {
        return indexRepository.getAllPageIdByLemmaId(lemmaId, listPageIdInSite);
    }

    public List<Object[]> getListFromPageIdAndCountRank(List<Integer> listPageId, List<Integer> listLemmaId) {
        return indexRepository.getListFromPageIdAndCountRank(listPageId, listLemmaId);
    }
}
