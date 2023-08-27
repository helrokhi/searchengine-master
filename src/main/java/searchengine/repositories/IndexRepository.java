package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;

@Repository
public interface IndexRepository extends JpaRepository<IndexEntity, Integer> {
    @Query(value =
            "SELECT id FROM `index` " +
                    "WHERE page_id= :pageId", nativeQuery = true)
    Iterable<Integer> findAllIndexIdByPageId(int pageId);

    @Query(value =
            "SELECT page_id FROM `index` " +
                    "WHERE lemma_id= :lemmaId AND page_id IN :listPageIdInSite", nativeQuery = true)
    List<Integer> getAllPageIdByLemmaId(int lemmaId, List<Integer> listPageIdInSite);

    @Query(value =
            "SELECT SUM(`rank`) FROM `index` " +
                    "WHERE page_id= :pageId AND lemma_id IN :listLemmaId", nativeQuery = true)
    float countRank(int pageId, List<Integer> listLemmaId);
}
