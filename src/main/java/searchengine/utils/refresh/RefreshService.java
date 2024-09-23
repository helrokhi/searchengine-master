package searchengine.utils.refresh;

import searchengine.model.SiteEntity;

import java.util.List;

public interface RefreshService {
    void refreshPageEntity(String link, List<SiteEntity> siteEntityList);

    boolean isLinkInSites(String link, List<SiteEntity> siteEntityList);
}
