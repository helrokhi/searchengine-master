package searchengine.services.gradations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.sitemaps.Page;
import searchengine.config.sites.Site;
import searchengine.model.LemmaEntity;
import searchengine.repositories.LemmaRepository;
import searchengine.services.sitemaps.SiteService;

import java.util.Collection;
import java.util.List;

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

    public LemmaEntity findLemmaByLemmaAndSiteId(String key, Site site) {
        int siteId = siteService.getSiteEntity(site).getId();
        return lemmaRepository.findByLemma(key, siteId);
    }

    public Integer findLemmaIdByLemmaAndSiteId(String key, int siteId) {
        return lemmaRepository.getLemmaId(key, siteId);
    }

    public int getCountLemmas() {
        return lemmaRepository.countAllLemmas();
    }

    public int getCountLemmasBySite(Site site) {
        return lemmaRepository.countAllLemmasBySiteId(siteService.getSiteEntity(site).getId());
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

    public void deleteLemmas(Site site) {
        int siteId = siteService.getSiteEntity(site).getId();
        List<Integer> list = (List<Integer>) lemmaRepository.findAllLemmasIdBySiteId(siteId);

        lemmaRepository.deleteAllByIdInBatch(list);
        System.out.println("\tLemmaService deleteLemmas" +
                " site " + site.getUrl() +
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
}
