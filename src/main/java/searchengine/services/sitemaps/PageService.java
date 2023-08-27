package searchengine.services.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.sites.Site;
import searchengine.model.PageEntity;
import searchengine.repositories.PageRepository;

import java.util.List;

@Service
public class PageService {
    private SiteService siteService;
    @Autowired
    private PageRepository pageRepository;

    public PageService(SiteService siteService) {
        this.siteService = siteService;
    }

    public PageEntity getPageById(Integer pageId) {
        return pageRepository
                .findById(pageId).orElse(null);
    }

    public PageEntity getPageByPath(String path) {
        return pageRepository
                .findByPath(path);
    }

    public int getCountPages() {
        return pageRepository.countAllPages();
    }

    public int getCountAllPagesBySite(Site site) {
        return pageRepository
                .countAllPagesBySiteId(siteService.getSiteEntity(site).getId());
    }

    public void savePageEntity(PageEntity pageEntity) {
        pageRepository.save(pageEntity);
        siteService.newStatusTime(siteService.getSiteById(pageEntity.getSiteId()));
    }

    public List<Integer> getListPageIdBySite(Site site) {
        int siteId = siteService.getSiteEntity(site).getId();
        return (List<Integer>) pageRepository.findAllPagesIdBySiteId(siteId);
    }

    public void deletePages(Site site) {
        System.out.println("\tPageService deletePages" +
                " site: " + site.getUrl() +
                " count: " + getListPageIdBySite(site).size() +
                " siteId: " + siteService.getSiteEntity(site).getId() +
                "");
        pageRepository.deleteAllByIdInBatch(getListPageIdBySite(site));
    }

    public void deletePage(int pageId) {
        System.out.print("\tPageService deletePage" +
                " pageId " + pageId +
                "");
        pageRepository.deleteById(pageId);
    }
}
