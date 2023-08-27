package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Integer> {
    @Query(value = "SELECT * FROM site WHERE url = :url", nativeQuery = true)
    SiteEntity findSiteByUrl(String url);

    @Query(value = "SELECT count(*) FROM site", nativeQuery = true)
    int countAllSites();

    int countAllByStatus(Status status);
}
