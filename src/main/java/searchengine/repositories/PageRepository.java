package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Integer> {
    @Query(value = "SELECT * from page WHERE path=?1", nativeQuery = true)
    PageEntity findByPath(String path);

    @Query(value = "SELECT count(*) FROM page", nativeQuery = true)
    int countAllPages();

    @Query(value = "SELECT count(*) FROM page WHERE site_id=?1", nativeQuery = true)
    int countAllPagesBySiteId(int siteId);

    @Query(value = "SELECT id FROM page WHERE site_id=?1", nativeQuery = true)
    Iterable<Integer> findAllPagesIdBySiteId(int siteId);
}
