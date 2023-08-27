package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

import java.util.Collection;
import java.util.List;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Integer> {
    @Query(value =
            "SELECT * FROM lemma WHERE lemma=?1 AND site_id=?2", nativeQuery = true)
    LemmaEntity findByLemma(String key, int siteId);

    @Query(value =
            "SELECT * FROM lemma " +
                    "WHERE site_id= :siteId AND lemma IN :words", nativeQuery = true)
    List<LemmaEntity> findByLemmas(int siteId, Collection<String> words);

    @Query(value =
            "SELECT id FROM lemma " +
                    "WHERE site_id= :siteId AND lemma IN :words", nativeQuery = true)
    Collection<Integer> findAllLemmaIdInLemma(int siteId, Collection<String> words);

    @Query(value =
            "SELECT lemma FROM lemma " +
                    "WHERE site_id= :siteId AND lemma IN :words", nativeQuery = true)
    Collection<String> findAllLemmaInLemma(int siteId, Collection<String> words);

    @Query(value = "SELECT id FROM lemma WHERE lemma=?1 AND site_id=?2", nativeQuery = true)
    int getLemmaId(String key, int siteId);

    @Query(value = "SELECT count(*) FROM lemma", nativeQuery = true)
    int countAllLemmas();

    @Query(value = "SELECT count(*) FROM lemma WHERE site_id=?1", nativeQuery = true)
    int countAllLemmasBySiteId(int siteId);

    @Query(value = "SELECT id FROM lemma WHERE site_id= :siteId", nativeQuery = true)
    Iterable<Integer> findAllLemmasIdBySiteId(int siteId);
}
