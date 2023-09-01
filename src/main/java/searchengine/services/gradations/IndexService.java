package searchengine.services.gradations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.gradations.Gradation;
import searchengine.model.IndexEntity;
import searchengine.repositories.IndexRepository;

import java.util.List;

@Service
public class IndexService {
    @Autowired
    private IndexRepository indexRepository;

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

    public float countRank(int pageId, List<Integer> listLemmaId) {
        return indexRepository.countRank(pageId, listLemmaId);
    }
}
