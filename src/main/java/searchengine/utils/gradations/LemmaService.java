package searchengine.utils.gradations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.sitemaps.Page;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.utils.sitemaps.SiteService;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LemmaService {
    private SiteService siteService;
    @Autowired
    private LemmaRepository lemmaRepository;

    public LemmaService(SiteService siteService) {
        this.siteService = siteService;
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
}
