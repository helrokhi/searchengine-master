package searchengine.utils.sitemaps;

import searchengine.config.sites.Site;

public interface RemoveService {
    void deleteAll(Site site);
    void deleteAllPage(SitePage page);
}
