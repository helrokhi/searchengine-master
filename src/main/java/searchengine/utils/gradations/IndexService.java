package searchengine.utils.gradations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.IndexEntity;
import searchengine.repositories.IndexRepository;

import java.util.Collection;
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

    public List<Object[]> getListFromPageIdAndCountRank(List<Integer> listPageId, List<Integer> listLemmaId) {
        return indexRepository.getListFromPageIdAndCountRank(listPageId, listLemmaId);
    }
}
