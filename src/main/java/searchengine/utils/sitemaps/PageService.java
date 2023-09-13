package searchengine.utils.sitemaps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
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

    public int getCountAllPagesBySiteEntity(SiteEntity siteEntity) {
        return pageRepository
                .countAllPagesBySiteId(siteEntity.getId());
    }

    public void savePageEntity(PageEntity pageEntity) {
        pageRepository.save(pageEntity);
        siteService.newStatusTime(siteService.getSiteById(pageEntity.getSiteId()));
    }

    public List<Integer> getListPageIdBySiteEntity(SiteEntity siteEntity) {
        return (List<Integer>) pageRepository.findAllPagesIdBySiteId(siteEntity.getId());
    }

    public void deletePages(SiteEntity siteEntity) {
        System.out.println("\tPageService deletePages" +
                " siteEntity: " + siteEntity.getUrl() +
                " count: " + getListPageIdBySiteEntity(siteEntity).size() +
                " siteId: " + siteEntity.getId() +
                "");
        pageRepository.deleteAllByIdInBatch(getListPageIdBySiteEntity(siteEntity));
    }

    public void deletePage(int pageId) {
        System.out.print("\tPageService deletePage" +
                " pageId " + pageId +
                "");
        pageRepository.deleteById(pageId);
    }
}
