package searchengine.services.refresh;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.utils.refresh.Refreshing;
import searchengine.dto.indexing.IndexResponse;
import searchengine.model.SiteEntity;
import searchengine.utils.methods.Methods;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RefreshingService {
    private final Refreshing refreshing;
    private final Methods methods;

    public IndexResponse getIndexPageResponse(String url) {
        List<SiteEntity> siteEntityList = methods.findAllSiteEntities();
        System.out.println("1. RefreshingService getIndexPageResponse" +
                " url " + url +
                " isLinkInSites " + (refreshing.isLinkInSites(url, siteEntityList)) +
                "");
        IndexResponse response = new IndexResponse();


        if (!refreshing.isLinkInSites(url, siteEntityList)) {
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, " +
                    "\nуказанных в конфигурационном файле");
            return response;
        } else {
            refreshing.refreshPageEntity(url, siteEntityList);
            response.setResult(true);
        }
        System.out.println("2. RefreshingService getIndexPageResponse " + response);
        return response;
    }
}
